import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.*;

class Estabelecimento {
    public Semaphore saida; //semáforo para controlar a saída dos clientes e garantir a atomocidade em mesaCheia e sentados
    public Semaphore sentar; //semáforo para garantir a atomocidade em mesaCheia e sentados
    public Semaphore vagas; //semáforo para controlar as vagas da mesa
    public String nome; //nome do estabelecimento
    public boolean mesaCheia; //booleano para saber se a mesa ficou cheia/lotada
    public int sentados; //quantidade de clientes sentados no momento
    public int totalVagas; //quantidade total de vagas que o restaurante suporta

    //método construtor do estabelecimento, que nesse caso é o restaurante
    public Estabelecimento(int numLugares, String nome) {
        //todos os semáforos irão respeitar a ordem de chegada (FIFO)
        this.saida = new Semaphore(1, true);
        this.sentar = new Semaphore(1, true);
        this.vagas = new Semaphore(numLugares, true);
        this.nome = nome;
        this.mesaCheia = false;
        this.sentados = 0;
        this.totalVagas = numLugares;
    }
}

class Cliente implements Runnable {
    private int tempoParaComer; //tempo em milisegundos que o cliente demora para jantar
    private Estabelecimento local; //local que o cliente irá (restaurante)

    //construindo o cliente que irá usar o estabelecimento
    public Cliente(int tempoParaComer, Estabelecimento local) {
        this.tempoParaComer = tempoParaComer;
        this.local = local;
    }

    public void jantar() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " chegou ao " + local.nome + " e está procurando um assento!");
        local.vagas.acquire(); //consegue um assento para sentar ou aguarda até que um assento fique livre (de 5 possíveis, nesse caso)
        local.sentar.acquire();
        local.sentados++; //incrementa a quantidade de clientes sentados
        System.out.println("***** " + Thread.currentThread().getName() + " achou uma vaga, sentou e começou a comer!");
        //se após conseguir um assento para sentar não houver mais assentos livre, isso significa que
        //estamos na condição de que a mesa está cheia (5 lugares ocupados)
        if(local.vagas.availablePermits() == 0) {
            local.mesaCheia = true; //flag de mesa cheia 
        }
        local.sentar.release();
        Thread.sleep(this.tempoParaComer); //tempo de simulação de consumo da janta
        local.saida.acquire();
        System.out.println("<<<<< " + Thread.currentThread().getName() + " acabou de comer e está indo embora.");
        local.sentados--;; //decrementa a quantidade de assentos que estão sendo usados
        //se a mesa esteve cheia
        if(local.mesaCheia) {
            //se o cliente for o último a sair da mesa que estava cheia, isso significa que
            //estamos na condição de que a mesa está vazia agora (0 lugares ocupados)
            if(local.sentados == 0) {
                local.mesaCheia = false;
                local.vagas.release(local.totalVagas); //libera todos os assentos para serem ocupados por novos clientes
            }
            //se a mesa estava cheia porém o cliente não é o último sair da mesa, ele não libera os assentos
        } else {
            //se a mesa não esteve cheia
            local.vagas.release(); //libera apenas o assento que estava sendo usado pelo cliente
        }
        local.saida.release();
        //ou seja
        //mesa cheia e cliente é o último: libera todos os assentos
        //mesa cheia e o cliente não é o último: não libera nenhum assento
        //mesa não cheia e o cliente é o último: libera apenas o seu assento
        //mesa não cheia e o cliente não é o último: libera apenas o seu assento
    }
    
    //ações a serem realizadas pelo cliente
    @Override
    public void run() {
        try {
            jantar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Restaurante {
    public static void main(String[] args) throws InterruptedException {
        //criação do restaurante
        Estabelecimento restaurante = new Estabelecimento(5, "Restaurante do Bom");
        //criação de uma lista de clientes que irão no restaurante
        List<Thread> clientes = new ArrayList<>();
        //quantidade de threads que serão rodadas no programa 
        int nrThreads = 17;
        //números aleatórios para causar maior diversidade de eventos no programa
        int tempoComer = 1000;

        //criação dos clientes e colocação na lista de clientes
        for(int i = 0; i < nrThreads; ++i) {
            clientes.add(new Thread(new Cliente(tempoComer, restaurante), "Cliente " + i));
        }

        //inicia todas as threads
        for (Thread t: clientes) {
            t.start();
        }
    }
}
