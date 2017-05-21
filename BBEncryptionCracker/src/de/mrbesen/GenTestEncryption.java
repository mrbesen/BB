package de.mrbesen;

import java.util.Random;

public class GenTestEncryption {
	
	public static void main(String[] args) {
		long max = 50000;
		long a, b, c, d, M, v, e, n;
		
		Random rand = new Random();
		a = rand.nextLong() % max;
		b = rand.nextLong() % max;
		c = rand.nextLong() % max;
		d = rand.nextLong() % max;
		
		
		//positive only
		a = (a > 0 ? a : -a);
		b = (b > 0 ? b : -b);
		c = (c > 0 ? c : -c);
		d = (d > 0 ? d : -d);
		
		M = (a*b)-1;
		v = (c*M) +a;
		e = (d*M) +b;
		n = (v*e)-1 / M;
		
		System.out.println("e: " + e + "\nv: " + v + "\nn: " + n);
		
		if((v*e) % n == 1) {
			System.out.println("Schlüßel gültig!");
		}
	}
}