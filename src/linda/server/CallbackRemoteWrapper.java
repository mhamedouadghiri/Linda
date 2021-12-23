package linda.server;

import linda.Callback;
import linda.Tuple;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CallbackRemoteWrapper extends UnicastRemoteObject implements CallbackRemote {

    private final Callback callback;

    public CallbackRemoteWrapper(Callback callback) throws RemoteException {
        this.callback = callback;
    }

    @Override
    public void call(Tuple t) throws RemoteException {
        callback.call(t);
    }
}
