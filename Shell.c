#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <errno.h>
#include <math.h>

char delimit[] = " \t\r\n\v\f";				// whitespace chars
int runningpids[20];	
int boo = 1;
void prompt(int commandCount);
void processInstruction(int argc, char *argv[]);
void executeInstruction(char *argv[], char *outR, char *inR, int background);
void redirectInput(char *file);
void redirectOutput(char *file);
void make_pipe1(char *pipe1[], char *argv[], int split);
void make_pipe2(char *pipe2[], char *argv[], int split, int end);
void printError();
void pwd();						// execution of pwd
void cd(char *dir);					// execution of cd
void clearzombies();
void exitshell();
void myPipe(char** first, char** second);
void run_pipe1(int fd[], char** arg1);
void run_pipe2(int fd[], char** arg2);
int commandcount = 1;

int main(int argc, char *argv[]){
	
	//badarg error checking
	if (argc > 1){
		printError();
		exit(1);
	}

	while(1){
		clearzombies();		
		prompt(commandcount);
		commandcount++;	
	}
	
	return 0;
}

// prompts user for input
void prompt(int commandCount){	
	char commandLine[1000];
	char *tmpargs[65];
	// print prompt, read in line of input to commandLine	
	printf("mysh (%i)> ", commandCount);
	fflush(stdout);
	if(fgets(commandLine, 1000, stdin) == (NULL)){
		return(printError());
	}
	// check for command line that is too long - print error message
	if(strlen(commandLine) > 128){
		return(printError());
	}
	
	char *token;
	int myargc = 0;
	token = strtok(commandLine, delimit);
	tmpargs[0] = token;
	// count number of args in commandLine
	// read each arg into tmpargs
	while(token != NULL){
		token = strtok(NULL, delimit);
		++myargc;
		tmpargs[myargc] = token;
	}
	
	// read Arguments into myargv
	char *myargv[myargc + 1];
	for(int lcv = 0; lcv < myargc; ++lcv){
		myargv[lcv] = tmpargs[lcv];
	}
	myargv[myargc] = NULL;

	// runs function to execute instruction, returns to main
	if(myargc > 0){
		processInstruction(myargc + 1, myargv);	
	} else {
		commandcount--;
	}
	free(token);
	token = NULL;
	return;
}

// executes instruction
void processInstruction(int argc, char *argv[]){
	char * blank = "";
	char * outputRedirect = NULL;
	char * inputRedirect = NULL;
	int endofinstruction = argc - 1;
	int endset = 0;
	int background = 0;
	
	// only redirect char, do nothing
	if(argc == 2){
		if(strcmp(argv[0], ">") == 0){
			return;
		}
		if(strcmp(argv[0], "<") == 0){
			return;
		}
	}

	// exit, cd, pwd cases
	if(strcmp(argv[0], "exit") == 0){
		if(argc > 2){
			return(printError());
		}
		exitshell();
		return;
	}
	if(strcmp(argv[0], "cd") == 0){
		if(argc == 2){
			cd(blank);
		}else{
			cd(argv[1]);
		}
		return;
	}
	if(strcmp(argv[0], "pwd") == 0){
		if(argc > 2){
			return(printError());
		}
		pwd();	
		return;	
	}

	// run in background
	if(strcmp(argv[argc-2], "&") == 0){
		background = 1;
	}

	// "|" ">" "<" cases
	for(int lcv = 0; lcv < argc-1; ++lcv){
		if(strcmp(argv[lcv], "|") == 0){
			// PIPE
			if(lcv == 0){		// no command before pipe
				return(printError());	
			}
			if(!(argv[lcv + 1])){	// no command after pipe
				return(printError());
			}
			if(!(argv[lcv - 1])){	// no command before pipe
				return(printError());
			}
		
		// create new arg vectors
		char *pipe1[lcv + 1];
		make_pipe1(pipe1, argv, lcv);
		char *pipe2[argc - lcv - 1];
		make_pipe2(pipe2, argv, lcv, argc);
		//run the pipe argument vectors
		myPipe(pipe1, pipe2);			
		return;
		}

		if(strcmp(argv[lcv], ">") == 0){
			// REDIRECT - OUTPUT
			if(!(argv[lcv + 1])){	// no file specified
				return(printError());
			}
			if((lcv + 2) < argc-1){	// too many args after redirect
				if(strcmp(argv[lcv + 2], "<") != 0){
					if(strcmp(argv[lcv + 2], "&") != 0){
						return(printError());
					}
				}
			}
			outputRedirect = argv[lcv + 1];
		
			// sets index of end of instruction to be executed	
			if(endset == 0){
				endofinstruction = lcv;
				endset = 1;
			}
		}

		if(strcmp(argv[lcv], "<") == 0){
			// REDIRECT - INPUT
			if(!(argv[lcv + 1])){	// no file specified
				return(printError());
			}
			if((lcv + 2) < argc-1){	// too many args after redirect
				if(strcmp(argv[lcv + 2], ">") != 0){
					if(strcmp(argv[lcv + 2], "&") != 0){
						return(printError());
					}
				}
			}
			inputRedirect = argv[lcv + 1];	

			// sets index of end of instruction to be executed	
			if(endset == 0){
				endofinstruction = lcv;
				endset = 1;
			}
		}
	}

	// if argv contains <, >, &: creates new argv w/out these chars 
	int newLength = 0;
	if(endset == 1){
		newLength = endofinstruction + 1;
	} else if(background == 1){
		newLength = argc - 1;
	} else {
		newLength = argc;
	}
	
	char *newargv[newLength];
	for(int lcv = 0; lcv < newLength - 1; ++lcv){
		newargv[lcv] = argv[lcv];
	}
	newargv[newLength - 1] = NULL;		
	executeInstruction(newargv, outputRedirect, inputRedirect, background);	

	return;
}

void executeInstruction(char *argv[], char *outR, char *inR, int background){
	pid_t pid, wpid;

	// create child process with fork() - check for errors
	if((pid = fork()) == -1){	// create child process
		return(printError());
	} else if(pid == 0){		// in child, execute process
	
		//input and output redirect cases
		if(outR){
			redirectOutput(outR);
		}
		if(inR){
			redirectInput(inR);
		}
		
		if((execvp(argv[0], argv)) == -1){
			printError();
			exit(0);
		}	
	} else{				// in parent
		// do not run in background - wait for child to execute
		if(background == 0){
			while(wpid != pid){
				if((wpid = waitpid(pid, NULL, WNOHANG)) == -1){
					return(printError());
				}
			}
		} else {
		// run in background
			for(int lcv = 0; lcv < 20; ++lcv){
				if(runningpids[lcv] == 0){
					runningpids[lcv] = pid;
					return;
				}
			}
			return(printError());
		}
	}

	return;
}

//used for reading from file
void redirectInput(char *file){
	int myFile;
	myFile = open(file, O_RDONLY);
	if(myFile == -1){	
		return(printError());
	}
	if((dup2(myFile, STDIN_FILENO)) == -1){
		return(printError());
	}
	if(close(myFile) == -1){
		return(printError());
	}
	return;	
}

//used for writing to file
void redirectOutput(char *file){
	int myFile;
	myFile = open(file, O_WRONLY|O_CREAT|O_TRUNC, 00700);
	if(myFile == -1){	
		return(printError());
	}
	if((dup2(myFile, STDOUT_FILENO)) == -1){
		return(printError());
	}
	if(close(myFile) == -1){
		return(printError());
	}
	return;	
}


//these two functions make the pipe arguments
//ex: input is "seq 5 | wc -c"
//output is seq 5 and wc -c
void make_pipe1(char *pipe1[], char *argv[], int split){
	for(int lcv = 0; lcv < split; ++lcv){
		pipe1[lcv] = argv[lcv];
	}
	pipe1[split] = NULL;
}

void make_pipe2(char *pipe2[], char *argv[], int split, int end){
	int length = end - split - 1;
	for(int lcv = 0; lcv - length; ++lcv){
		pipe2[lcv] = argv[split + lcv + 1];
	}
}

void pwd(){
	char *out = getcwd(NULL, 0);
	if(out == NULL){
		return(printError());
	}
	if(write(STDOUT_FILENO, out, strlen(out)) == (-1)){
		return(printError());
	}
	if(write(STDOUT_FILENO, "\n", 1) == (-1)){
		return(printError());
	}
	free(out);	
	out = NULL;
	return;
}

void cd(char *dir){
	if(strlen(dir) == 0){
		dir = getenv("HOME");
	}
	int test = chdir(dir);
	if(test == -1){
		return(printError());
	}
	return;
}


void printError(){
	char error_message[30] = "An error has occurred\n";
	if(write(STDERR_FILENO, error_message, strlen(error_message)) == (-1)){
		return;
	}
}

void clearzombies(){
	pid_t zpid;

	for(int lcv = 0; lcv < 20; ++lcv){
		if(runningpids[lcv] != 0){
			// process found - check status
			if((zpid = (waitpid(runningpids[lcv], NULL, WNOHANG))) == -1){
				return(printError());
			} else if(zpid == runningpids[lcv]){
				// child has finish execution - clear from array
				runningpids[lcv] = 0;
			}
		}
	}
	return;
}


void exitshell(){
	for(int lcv = 0; lcv < 20; ++lcv){
		if(runningpids[lcv] != 0){
			if(kill(runningpids[lcv], SIGKILL) == -1){
				return(printError());
			}
		}
	}
	exit(0);
	
	return;
}

//manages the piping process
void myPipe(char** first, char** second){
	int pid, status;
	int fd[2];
	pipe(fd);
	run_pipe1(fd, first);
	run_pipe2(fd, second);
	close(fd[0]); close(fd[1]); 	
	while ((pid = wait(&status)) != -1){	
	}
}

//redirects and runs the first half of the pipe argument
void run_pipe1(int fd[], char** arg1)	{

	int child = fork();
	if (child == -1){
		printError();
		exit(1);
	} else if (child == 0) {
		dup2(fd[1], 1);
		close(fd[0]); 	
		execvp(arg1[0], arg1);	
		printError();
		exit(0);		

	}
}

//redirects and runs the second half of the pipe argument
void run_pipe2(int fd[], char** arg2){
	int child = fork();
	if (child == -1){
		printError();
		exit(1);
	} else if (child == 0) {
		dup2(fd[0], 0);
		close(fd[1]); 	
		execvp(arg2[0], arg2);	
		printError();
		exit(0);		

	}
}
