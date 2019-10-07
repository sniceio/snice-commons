/**
 * 
 */
package io.snice.time;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class SystemClock implements Clock {

    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

}
