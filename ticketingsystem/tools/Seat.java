package ticketingsystem.tools;

import ticketingsystem.MyTicket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Seat {
    private Lock lock;
    private BitMap bitMap;

    public Seat(int stationNum) {
        this.lock = new ReentrantLock();
        this.bitMap = new BitMap(stationNum);
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }

    public void occupy(int s, int e) {
        int sIdx = s - 1;
        int eIdx = e - 1;
        for (int i = sIdx; i < eIdx; i++) {
            bitMap.set(i);
        }
    }

    public void free(int s, int e) {
        int sIdx = s - 1;
        int eIdx = e - 1;
        for (int i = sIdx; i < eIdx; i++) {
            bitMap.reset(i);
        }
    }

    public boolean isAvailable(MyTicket ticket) {
        int sIdx = ticket.departure - 1;
        int eIdx = ticket.arrival - 1;
        long[] bm = bitMap.rawSnapshot();
        for (int i = sIdx; i < eIdx; i++) {
            int arrayIdx = i / 64;
            int stationIdx = i % 64;
            if (BitHelper.getBit(bm[arrayIdx], stationIdx)) {
                return false;
            }
        }
        return true;
    }
}