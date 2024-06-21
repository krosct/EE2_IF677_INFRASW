import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class Estabelecimento {
    private Semaphore lugares;
    private int clientesSentados;
    private int lotacao;

    public Estabelecimento(int numLugares) {
        this.lotacao = numLugares;
        this.lugares = new Semaphore(numLugares, true);
        this.clientesSentados = 0;
    }

    public void entrarEstabelecimento() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " chegou ao restaurante.");
        lugares.acquire();
        this.clientesSentados++;
        System.out.println(Thread.currentThread().getName() + " sentou para jantar.");
        System.out.println(Thread.currentThread().getName() + " começou a jantar.");
        Thread.sleep(3000); // Simula o tempo de jantar
        sairEstabelecimento();
    }

    public void sairEstabelecimento() throws InterruptedException {
        this.clientesSentados--;
        System.out.println(Thread.currentThread().getName() + " levantou da mesa.");
        if (clientesSentados == 0) {
            System.out.println("Todos os clientes saíram. Próximo grupo pode entrar para jantar.");
            lugares.release(5);
        }
    }
}

class Cliente implements Runnable {
    private Estabelecimento restaurante;

    public Cliente(Estabelecimento restaurante) {
        this.restaurante = restaurante;
    }

    @Override
    public void run() {
        try {
            restaurante.entrarEstabelecimento();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Restaurante {
    public static void main(String[] args) {
        Estabelecimento restaurante = new Estabelecimento(5);
        List<Thread> clientes = new ArrayList<>();

        clientes.add(new Thread(new Cliente(restaurante), "Cliente1"));
        clientes.add(new Thread(new Cliente(restaurante), "Cliente2"));
        clientes.add(new Thread(new Cliente(restaurante), "Cliente3"));
        clientes.add(new Thread(new Cliente(restaurante), "Cliente4"));
        clientes.add(new Thread(new Cliente(restaurante), "Cliente5"));
        clientes.add(new Thread(new Cliente(restaurante), "Cliente6"));
        clientes.add(new Thread(new Cliente(restaurante), "Cliente7"));
        clientes.add(new Thread(new Cliente(restaurante), "Cliente8"));

        // Inicia todas as threads
        for (Thread t: clientes) {
            t.start();
        }

        // Espera todas as threads terminarem
        for (Thread t: clientes) {
            try {
                t.join();
            } catch (InterruptedException e) {
                t.interrupt();
            }
        }

        System.out.println("Restaurante fechando...");
    }
}
