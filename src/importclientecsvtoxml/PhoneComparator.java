/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package importclientecsvtoxml;

import java.util.Comparator;

/**
 *
 * @author ASIR\xavi
 */
public class PhoneComparator implements Comparator <Long> {
    @Override
    public int compare(Long arg0, Long arg1) {
        return (int)(arg0-arg1);
    }
    
}
