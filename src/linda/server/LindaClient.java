package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 */
public class LindaClient implements Linda {

    private static LindaRemote server;

    /**
     * Initializes the Linda implementation.
     *
     * @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        try {
            server = (LindaRemote) Naming.lookup(serverURI);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Tuple t) {
        try {
            server.write(t);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return server.take(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return server.read(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return server.tryTake(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return server.tryRead(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return server.takeAll(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return server.readAll(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        try {
            server.eventRegister(mode, timing, template, new CallbackRemoteWrapper(callback));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void debug(String prefix) {
        try {
            server.debug(prefix);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
