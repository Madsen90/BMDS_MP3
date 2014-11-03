public enum CodeType{
            Error, Connecting, ConnectionEstablished, 
            Put, Get, Success, Heartbeat, Panic, Backup, Delete, PanicJoin, DeleteBackup
        }
        	/*
		-1 = Error
		1 = Connecting To Network
		2 = ConnectionEstablished
		3 = Put
		4 = Get
		5 = Success
                6 = Heartbeat
                7 = Panic
		8 = Backup
	*/