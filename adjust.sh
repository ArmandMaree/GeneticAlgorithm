#!/bin/bash

function recursiveSearch {
	FILES=$1

	for f in $FILES
	do
		if [ ! -f $f ]; then
			if [ -d $f ]; then
				if [ ! -e $2$f ]; then
					mkdir $2$f;
				fi

				recursiveSearch $f"/*" "/home/armandmaree/TUKS/COS314/Project3/images/";
			fi
		else
			#newName=$(echo $f | cut -f 1 -d '.')
			convert $f -resize 300x300! $2$f
			echo "Done: "$f
		fi
	done
}

recursiveSearch "*" "/home/armandmaree/TUKS/COS314/Project3/images/"
