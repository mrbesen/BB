#!/bin/python
public=14012539
mod=4862802614
step = mod / 100
for j in range(0, 100):
    print "Testing from", step*j, " to ", step*(j+1), " (", j, "%)"
    for i in range(j*step, (j+1)*step):
        if (( i * public ) % mod) == 1:
             print i
             break
