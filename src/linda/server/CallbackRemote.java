package linda.server;

import linda.Tuple;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackRemote extends Remote {

    void call(Tuple t) throws RemoteException;
}
