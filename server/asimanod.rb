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

TOKEN_LIST_FILE = '/home/masm/asimano/tokens.txt'

SERVER_KEY = ''

END_POINT = 'https://fcm.googleapis.com/fcm/send'

def send_notification(nr_unread)
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

def checkdir(path, re)
  nr = 0
  Dir.open(path) do |dir|
    dir.each do |name|
      next if name == '.'
      next if name == '..'
      nr += 1 if re =~ name
    end
  end
  return nr
end

@cur_nr = -1
@need_notify = false
def checkdirs
  nr = 0
  nr += checkdir("#{ENV['HOME']}/Maildir/new", /./)
  nr += checkdir("#{ENV['HOME']}/Maildir/cur", /:2,[^S]*$/)
  if nr != @cur_nr
    @cur_nr = nr
    @need_notify = true
  end
end

proc = Proc.new { |req, res|
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
      f.write "#{k}\t#{v}\n"
    end
  end
  begin
    File.unlink TOKEN_LIST_FILE
  rescue Exception
  end
  File.rename "#{TOKEN_LIST_FILE}.new", TOKEN_LIST_FILE

  checkdirs
  send_notification(@cur_nr)
}

fork do
  srv = WEBrick::HTTPServer.new({ DocumentRoot: '/',
                                  BindAddress: '127.0.0.1',
                                  Port: 18080 })
  srv.mount_proc('/asimano/register', proc)
  srv.start
end

fork do
  checkdirs
  
  notifier = INotify::Notifier.new
  notifier.watch("#{ENV['HOME']}/Maildir/new", :create, :delete, :move) do
    checkdirs
  end
  notifier.watch("#{ENV['HOME']}/Maildir/cur", :create, :delete, :move) do
    checkdirs
  end
  
  while true
    if IO.select([notifier.to_io], [], [], 2)
      notifier.process
    else
      if @need_notify
        send_notification(@cur_nr)
        @need_notify = false
      end
    end
  end
end
