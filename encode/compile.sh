#! /bin/bash

# Simple script to compile each resource using abrc and then encode into a base64 representation
# Note, your resources should NOT have spaces or the script won't work

# TODO: cleanup and make better ninepatch compiler

#up one directory..
PROJECT_DIR=".."

OUTPUT_DIR="output"

rm -rf $OUTPUT_DIR

#hack to loop through files (and dance around space in filename issue)
#we use -E option to allow extended regex
#Note: bit wasteful since we run abrc for /each/ file. Instead, should just make long output list at once when calling abrc.
find -E ${PROJECT_DIR}/res -regex ".*\.(jpg|png|jpeg)" | while read filename
do
	#make the filename relative to the root project directory (get rid of PROJECT_DIR prefix)
	clean_filename=${filename#$PROJECT_DIR/}

	#run Android Binary Resource Compiler
	echo "Compiling $clean_filename"
	abrc compile $PROJECT_DIR $OUTPUT_DIR "$clean_filename"
	
	#compiled resources will be local to $output_dir+$clean_filename in this directory
	#see docs for abrc
	
	#remove path ext then append new one
	fname_no_ext=${clean_filename%.*}
	base64_fname="$OUTPUT_DIR/${fname_no_ext}.b64"
	
	echo "Converting $clean_filename to base64 file $base64_fname"
	
	#relative to generated zip root
	base64 -e "$OUTPUT_DIR/$clean_filename" > $base64_fname
	
	#TODO: remove newlines in b64 lines?
done