#!/usr/bin/env ruby
#
#  ASimano - Simple Mail Notification for Android.
#  Copyright (C) 2018 Yuuki Harano
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.

require 'date'
require 'net/https'
require 'uri'
require 'json'
require 'webrick'
require 'rb-inotify'

require_relative './conf'

if TOKEN_LIST_FILE.nil?
  raise 'TOKEN_LIST_FILE not set.'
end
if SERVER_KEY.nil?
  raise 'SERVER_KEY not set.'
end

class Notifier
  
  END_POINT = 'https://fcm.googleapis.com/fcm/send'
  
  def notify(nr_unread)
    uri = URI.parse(END_POINT)
    
    header = {
      'Content-Type' => 'application/json',
      'Authorization' => 'key=' + SERVER_KEY,
    }
    
    retry_ctr = 0
    begin
      tokens = File.read(TOKEN_LIST_FILE).split(/\r?\n/).map{|s| s.split[0]}
    rescue Exception => e
      sleep 1
      retry_ctr += 1
      raise e if retry_ctr >= 3
      retry
    end
    
    tokens.each do |token|
      h = {
        to: token,
        data: {
          'stamp' => DateTime.now.new_offset(1.0 / 24 * 9).iso8601(6),
          'unread' => nr_unread.to_s,
        },
      }
      
      json = JSON.dump(h)
      
      https = Net::HTTP.new(uri.host, uri.port)
      https.use_ssl = true
      https.ca_path = '/etc/ca-certificates/extracted/cadir'
      https.verify_mode = OpenSSL::SSL::VERIFY_PEER
      https.start {
        res = https.post(uri.path, json, header)
        puts res
        puts res.body
      }
    end
  end
  
end

class DirChecker
  
  def initialize
    @cur_nr = -1
    @need_notify = false
  end
  
  def check
    nr = 0
    nr += checkdir("#{ENV['HOME']}/Maildir/new", /./)
    nr += checkdir("#{ENV['HOME']}/Maildir/cur", /:2,[^S]*$/)
    if nr != @cur_nr
      @cur_nr = nr
      @need_notify = true
    end
  end
  
  def cur_nr
    @cur_nr
  end
  
  def need_notify
    @need_notify
  end
  
  def reset
    @need_notify = false
  end
  
  private
  
  def checkdir(path, re)
    Dir.open(path) do |dir|
      dir.select{|name|
        case name
        when '.', '..'
          false
        when re
          true
        else
          false
        end
      }.count
    end
  end
  
end

class Registerer

  def initialize
    @dir_checker = DirChecker.new
    @notifier = Notifier.new
    
    @srv = WEBrick::HTTPServer.new({ DocumentRoot: '/',
                                    BindAddress: '127.0.0.1',
                                    Port: 18080 })
    @srv.mount_proc('/asimano/register') do |req, res|
      serve req, res
    end
  end
  
  def run
    @srv.start
  end
  
  private
  
  def serve(req, res)
    json = req.body
    h = JSON.load(json)
    token = h['token']
    puts token
    
    tokens = {}
    File.read(TOKEN_LIST_FILE).split(/\r?\n/).each do |line|
      k, v = line.split
      tokens[k] = v
    end
    tokens[token] = DateTime.now.new_offset(1.0 / 24 * 9).iso8601(6)
    File.open("#{TOKEN_LIST_FILE}.new", 'w') do |f|
      tokens.each_pair do |k, v|
        f.print "#{k}\t#{v}\n"
      end
    end
    
    begin
      File.unlink TOKEN_LIST_FILE
    rescue Exception
    end
    File.rename "#{TOKEN_LIST_FILE}.new", TOKEN_LIST_FILE
    
    @dir_checker.check
    @notifier.notify(@dir_checker.cur_nr)
  end
  
end

fork do
  srv = Registerer.new
  srv.run
end

class Watcher
  
  def initialize
    @dir_checker = DirChecker.new
    @dir_checker.check
    
    @inotifier = INotify::Notifier.new
    @inotifier.watch("#{ENV['HOME']}/Maildir/new", :create, :delete, :move) do
      @dir_checker.check
    end
    @inotifier.watch("#{ENV['HOME']}/Maildir/cur", :create, :delete, :move) do
      @dir_checker.check
    end
    
    @notifier = Notifier.new
  end
  
  def run
    while true
      if IO.select([@inotifier.to_io], [], [], 2)
        @inotifier.process
        
        if @dir_checker.need_notify && @dir_checker.cur_nr == 0
          @notifier.notify(@dir_checker.cur_nr)
          @dir_checker.reset
        end
      else
        if @dir_checker.need_notify
          @notifier.notify(@dir_checker.cur_nr)
          @dir_checker.reset
        end
      end
    end
  end

end

fork do
  w = Watcher.new
  w.run
end
