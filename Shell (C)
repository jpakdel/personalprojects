#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <math.h>


char** first_pipe(char** args, int size);
char** sec_pipe(char** args, int size);
char* change_my_string(char* cha);
void piper(char** args1, char** args2);
int main(int argc, char* argv[]){
	int j = 2;
	char* a[2];
	a[0] = strdup("ls");
	a[1] = NULL;
	
 	FILE* f = fopen("test.txt", "w");
	int fn = fileno(f);
	
	char** input = malloc(sizeof(char*)*5);
	input[0] = strdup("seq");
	input[1] = strdup("5");
	input[2] = strdup("|");	
	input[3] = strdup("wc");
	input[4] = strdup("-c");
	
	char** first = first_pipe(input, 5);
	
	char** second = sec_pipe(input, 5);
	
	piper(first, second);
	
		
	return 0;
}

void pwd(){
	char* out = getcwd(NULL, 0);
	if (out == NULL) { myError();}
	write(STDOUT_FILENO, out, strlen(out));
	write(STDOUT_FILENO, "\n", 1);
	fflush(stdout);
}

void cd(char *dir){
	if (strlen(dir)==0){
		dir = getenv("HOME");
	}

	int test = chdir(dir);

	if (test == -1){
		myError();
	}
}

void myError(){
	char* out = "An error has occurred\n";
	write(STDERR_FILENO, out, strlen(out));
}

void prompt(int i){
	printf("Shell (%i)> ", i);
	fflush(stdout);
}

//accepts filename and a number, 0 for output/writing
//1 for input/reading and redirects the stdout/stdin
void coolPrint(char* fileName, int num){
	FILE *file = NULL;

	if (num ==0){
	file = fopen(fileName, "w");
	
	if (file == NULL){myError();}
	int fn = fileno(file);
	dup2(fn, fileno(stdout));
	} else {
	file = fopen(fileName, "r");
	
	if (file == NULL){myError();}
	int fn = fileno(file);
	dup2(fn, fileno(stdin));
	
	}
	
	
}

//accepts array of arguments and does appropriate exec call
void os(char** cha){
	char* first;
	first=change_my_string(cha[0]);
	cha[0] = first;
	
	int  child = fork();
	int stat;
	if (child == -1){
		myError();
		exit(0);
	}

	if (child == 0){
		execvp(cha[0], cha);
	} else { 
		waitpid(child, &stat, 0);
	}
	
}

//accepts array of arguments and integer for number of arguments
//ex: takes in "seq 5 | wc -c" and returns an array with {"seq", "5"}
char** first_pipe(char** args, int size){
	int index =0;
	for (int i = 0; i < size; i++){
		if (args[i][0] == '|'){
			index = i;
			i = size;
		}
	}

	char** first = malloc((index+1)*sizeof(char*));
	
	for (int i = 0; i < index; i++){		
		first[i] = strdup(args[i]);
	}
	first[index] = NULL;
	
	char* b = change_my_string(first[0]);
	first[0] = b;
	
	return first;
}

//accepts array of arguments and integer for number of arguments
//ex: takes in "seq 5 | wc -c" and returns an array with {"wc", "-c"}
char** sec_pipe(char** args, int size){
	int index =0;
	
	for (int i = 0; i < size; i++){
		if (args[i][0] == '|'){
			index = i;
			i = size;
		}
	}
	
	int sec_size = size-index;
	
	char** second = malloc((sec_size)*sizeof(char*));
	for (int i = 0; i < sec_size-1; i++){
		second[i] = strdup(args[index+1+i]);
	}
	second[sec_size-1] = NULL;
	char *a = change_my_string(second[0]);
	
	second[0] = a;	
	return second;
}

//accepts two arrays of arguments and does the appropriate pipe and exec instructions
//ex: takes in "seq 5" and "wc -c" and returns 10
void piper(char** args1, char** args2){
	pid_t child1 = fork();
	if (child1 == -1){
		myError();
		exit(0);
	}
	pid_t child2;
	int stat;
	if (child1 == 0){
		int fd[2];
		pipe(fd);
		child2 = fork();
		if (child2 == -1){
			myError();
			exit(0);
		}
		if (child2 == 0){
			close(fd[1]);
			dup2(fd[0], fileno(stdin));
			if ((execvp(args2[0], args2)) == -1){
				myError();
				exit(0);
			}
		} else { 
			close(fd[0]);
			dup2(fd[1], fileno(stdout));
			if ((execvp(args1[0], args1)) == -1){
				myError();
				exit(0);
			}
		}
	} else { 
		waitpid(child2, &stat, 0);
		waitpid(child1, &stat, 0); 
	}

}
char* change_my_string(char* cha){
	
	char* first = strdup("");
	if((( strcmp(cha, "ls")) == 0) || ((strcmp(cha, "ps")) == 0 )){
		strcpy(first, "/bin/");

	} else if( (strcmp(cha, "head") == 0) ||  (strcmp(cha, "wc") == 0) || 
		(strcmp(cha, "seq") == 0) ||  (strcmp(cha, "yes") == 0) ||  (strcmp(cha, "find") == 0)){
		strcpy(first, "/usr/bin/");
	}

	strcat(first, cha);

	return first;

}

