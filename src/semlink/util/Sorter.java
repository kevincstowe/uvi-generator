package semlink.util;

import java.io.File;

public class Sorter {
	public static void sortFileArray(File[] files) {
      for( int a = 0; a < files.length - 1; a++ )
          for( int b = a + 1; b < files.length; b++ ) {
             File af = files[ a ];
             File bf = files[ b ];

             String an = files[ a ].getName();
             String bn = files[ b ].getName();

             if( an.compareTo( bn ) > 0 ) {
                files[ a ] = bf;
                files[ b ] = af;
             }
          }
	}
}
