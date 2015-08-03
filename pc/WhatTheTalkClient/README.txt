How to compile?
1. Change directory to pc/WhatTheTalkClient
2. Run "javac -sourcepath src -d bin src\com\wtt\client\Main.java"

How to use? 
** You must start WhatTheTalkServer first.(refer to pc/WhatTheTalkServer/readme.txt)
1. Change directory to pc/WhatTheTalkClient
2. Run "java -cp bin com.wtt.client.Main [IP [PORT]]"
3. You can send messages by entering anything except "exit".
4. Close this program by entering "exit"
** Default IP is 127.0.0.1 and PORT is 5566.