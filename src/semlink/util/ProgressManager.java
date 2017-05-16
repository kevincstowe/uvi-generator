package semlink.util;

public class ProgressManager {
    static double goal = 0.025;
    static int XsPrinted = 0;
    public static void reset() {
        XsPrinted = 0;
        goal = 0.025;
        System.out.print( "[" );
    }
    public static void next(int cur, int tot) {
        double pct = ( double ) cur / tot;

        // If the current goal has been reached, increase the goal
        // and print a progress marker.
        if( pct > goal )
        {
           goal += 0.025;

           System.out.print( "X" );

           XsPrinted++;
        }
    }
    public static void finish() {
        while(XsPrinted < 40) {
            System.out.print("X");
            XsPrinted++;
        }
       System.out.println( "]" );
    }
}
