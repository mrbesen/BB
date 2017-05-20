#!/bin/bash
public="14012539"
mod="4862802614"
for i in `seq 1 $mod`
do
   let out=( $i * $public ) % $mod
   if [ $out == 1 ]
    then
         echo "$i true"
         exit 0
     else
         echo "$i false"
   fi
done
