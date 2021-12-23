package linda.server;

import linda.Linda;
import linda.Tuple;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface LindaRemote extends Remote {

    void write(Tuple t) throws RemoteException;

    Tuple take(Tuple template) throws RemoteException;

    Tuple read(Tuple template) throws RemoteException;

    Tuple tryTake(Tuple template) throws RemoteException;

    Tuple tryRead(Tuple template) throws RemoteException;

    Collection<Tuple> takeAll(Tuple template) throws RemoteException;

    Collection<Tuple> readAll(Tuple template) throws RemoteException;

    void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, CallbackRemote callbackRemote) throws RemoteException;

    void debug(String prefix) throws RemoteException;
}
