package linda.server;

import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

public class LindaServer extends UnicastRemoteObject implements LindaRemote {

    private final Linda linda;

    public LindaServer() throws RemoteException {
        linda = new CentralizedLinda();
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(7878);

            LindaServer server = new LindaServer();
            String uri = "//localhost:7878/server";

            Naming.rebind(uri, server);

            System.out.println("Server set. URI: " + uri);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Tuple t) throws RemoteException {
        linda.write(t);
    }

    @Override
    public Tuple take(Tuple template) throws RemoteException {
        return linda.take(template);
    }

    @Override
    public Tuple read(Tuple template) throws RemoteException {
        return linda.read(template);
    }

    @Override
    public Tuple tryTake(Tuple template) throws RemoteException {
        return linda.tryTake(template);
    }

    @Override
    public Tuple tryRead(Tuple template) throws RemoteException {
        return linda.tryTake(template);
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException {
        return linda.takeAll(template);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) throws RemoteException {
        return linda.readAll(template);
    }

    @Override
    public void eventRegister(Linda.eventMode mode, Linda.eventTiming timing, Tuple template, CallbackRemote callbackRemote) throws RemoteException {
        linda.eventRegister(mode, timing, template, tuple -> {
            try {
                callbackRemote.call(tuple);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void debug(String prefix) throws RemoteException {
        linda.debug(prefix);
    }
}
