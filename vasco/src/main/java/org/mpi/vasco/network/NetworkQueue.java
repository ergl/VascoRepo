/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mpi.vasco.network;

/**
 *
 * @author manos
 */
public interface NetworkQueue {
    abstract public void addWork(byte[] msg);
}
