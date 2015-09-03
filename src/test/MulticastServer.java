/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

public class MulticastServer {
    public static void main(String[] args) throws java.io.IOException {
         new MulticastServerThread().start();
    }
}