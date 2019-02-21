import java.util.*;
public class validIPAddress {


    public static boolean isDigit(char ch){

        if (ch >= '0' && ch <= '9')
        {
            return true;
        }
        return false;
    }



    public static boolean isValidIp(char[] chArr) {
        int countNums =0;
        int countDots =0;
        int num =0;
        for ( char ch : chArr) {

            if(isDigit(ch)) {
                num = num * 10 + ch - '0';
                if (num < 0 || num > 255) {
                    return false;
                }
            }


            if (!isDigit(ch) && ch == '.') {


                    //System.out.println(num);
                    countDots++;
                    if (countDots >3) {
                        return false;
                    }
                   // System.out.println(countDots);
                    num = 0;
                    countNums++;
                    if (countNums >4){
                        return false;
                    }

            }

            if(!isDigit(ch) && ch != '.') {
                return false;
            }


        }


        return true;

    }

    public static void main (String[] args) {


        //String str = "127.0.0.0.";
        //String str = "127.0.0.0";
        //String str = "127.0.0.poonam.";
        //String str = "127.0.0.256";
        //String str = "0000.127.0.0";
        String str = "...127.0.0.0.";
        char[] chArr = str.toCharArray();
        System.out.println(isValidIp(chArr));
        //char ch = '6';
        //System.out.println(isDigit(ch));

    }




}
