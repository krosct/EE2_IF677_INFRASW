import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.*;

class Estabelecimento {
    private Semaphore lugaresDisponiveis; //quantidade de assentos vazios
    private int lugaresOcupados; //quantidade de assentos já ocupados por clientes
    private int vagas; //quantidade total de vagas que o restaurante suporta
    private String nome; //nome do estabelecimento
    private boolean mesaCheia; //booleano para saber se a mesa ficou cheia/lotada
    private final Lock lock = new ReentrantLock(); //locker para operações que devem ser atômicas

    //construindo o estabelecimento que nesse caso é um restaurante
    public Estabelecimento(int numLugares, String nome) {
        this.lugaresDisponiveis = new Semaphore(numLugares, true); //semáforo para controlar a quantidade de assentos vazios.
                                                                        //o semáforo irá respeitar a ordem de chegada (FIFO)
        this.lugaresOcupados = 0; //inicialmente o número de lugares ocupados por clientes é zero
        this.vagas = numLugares; //é posto a quantidade total de vagas que o restaurante suporta
        this.nome = nome; //nome do estabelecimento
        this.mesaCheia = false; //booleano para saber se a mesa ficou cheia/lotada
    }

    //retorna o atributo semáforo lugaresDisponiveis
    public Semaphore getLugaresDisponiveisSemaphore() {
        return this.lugaresDisponiveis;
    }

    //retorna o atributo nome
    public String getNome() {
        return this.nome;
    }

    //retorna o atributo lugaresOcupados
    public int getLugaresOcupados() {
        return this.lugaresOcupados;
    }

    //decrementa o atributo lugaresOcupados de forma atômica
    public void liberarAssento() {
        lock.lock();
        try {
            this.lugaresOcupados = this.lugaresOcupados - 1; //liberação de um assento, descremento dos lugares ocupados
        } finally {
            lock.unlock();
        }
    }

    //incrementa o atributo lugaresOcupados de forma atômica
    public void ocuparAssento() {
        lock.lock();
        try {
            this.lugaresOcupados = this.lugaresOcupados + 1; //ocupação de um assento, incremento dos lugares ocupados
        } finally {
            lock.unlock();
        }
    }

    //retorna o atributo vagas
    public int getVagas() {
        return this.vagas;
    }

    //retorna o atributo mesaCheia
    public boolean getMesaCheia() {
        return this.mesaCheia;
    }

    //define o atributo mesaCheia como true
    public void setMesaCheia() {
        lock.lock();
        try {
            this.mesaCheia = true; //a mesa ficou cheia
        } finally {
            lock.unlock();
        }
    }

    //define o atributo mesaCheia como false
    public void setMesaVazia() {
        lock.lock();
        try {
            this.mesaCheia = false; //a mesa volta para o estado de vazia
        } finally {
            lock.unlock();
        }
    }
}

class Cliente implements Runnable {
    private int tempoParaComer; //tempo em milisegundos que o cliente demora para jantar
    private Estabelecimento local; //local que o cliente irá (restaurante)
    private int startDelay; //tempo em miliseguntos que o cliente demora para ir para o estabelecimento

    //construindo o cliente que irá usar o estabelecimento
    public Cliente(int tempoParaComer, Estabelecimento local, int startDelay) {
        this.tempoParaComer = tempoParaComer; //tempo em milisegundos que o cliente demora para jantar
        this.local = local; //local que o cliente irá (restaurante)
        this.startDelay = startDelay; //tempo em miliseguntos que o cliente demora para ir para o estabelecimento
    }

    //apenas informativo
    public void entrarEstabelecimento() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " chegou ao estabelecimento " + local.getNome() + ".");
    }

    //o cliente procura um assento livre para sentar
    public void procurarLugarESentar() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " está procurando um assento.");
        local.getLugaresDisponiveisSemaphore().acquire(); //consegue um assento para sentar ou aguarda até que um assento fique livre
        local.ocuparAssento(); //incrementa a quantidade de assentos que estão sendo usados
        System.out.println("***** " + Thread.currentThread().getName() + " achou uma mesa com vaga!");
        //se após conseguir um assento para sentar não houver mais assentos livre, isso significa que
        //estamos na condição de que a mesa está cheia (5 lugares ocupados)
        if(local.getLugaresDisponiveisSemaphore().availablePermits() == 0) {
            local.setMesaCheia();
        }
    }

    //apenas informativo
    public void sentar() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " sentou para jantar.");
    }

    //apenas informativo, o cliente espera um tempo para simular que está comendo
    public void consumirJantar() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " começou a comer.");
        Thread.sleep(this.tempoParaComer);
    }

    //após comer, o cliente tem que conferir algumas situações antes de ir embora
    public void irEmbora() {
        System.out.println("<<<<< " + Thread.currentThread().getName() + " acabou de comer e está indo embora.");
        //se a mesa estiver cheia
        if(local.getMesaCheia()) {
            //se o cliente for o último a sair da mesa que estava cheia, isso significa que
            //estamos na condição de que a mesa está vazia (0 lugares ocupados)
            if(local.getLugaresOcupados() == 1) {
                System.out.println(Thread.currentThread().getName() + " foi o último a sair do grupo que lotou a mesa.");
                local.setMesaVazia();
                local.getLugaresDisponiveisSemaphore().release(local.getVagas()); //libera todos os assentos para serem ocupados por novos clientes
            }
            //se a mesa estava cheia porém o cliente não é o último sair da mesa, ele não libera os assentos
        } else {
            local.getLugaresDisponiveisSemaphore().release(); //libera o assento que estava sendo usado pelo cliente
        }
        local.liberarAssento(); //decrementa a quantidade de assentos que estão sendo usados
    
        //ou seja
        //mesa cheia e cliente é o último: libera todos os assentos
        //mesa cheia e o cliente não é o último: não libera nenhum assento
        //mesa não cheia e o cliente é o último: libera apenas o seu assento
        //mesa não cheia e o cliente não é o último:libera apenas o seu assento
    }

    //ações a serem realizadas pelo cliente
    @Override
    public void run() {
        try {
            System.out.println("----- Iniciando " + Thread.currentThread().getName() + ". SD: " + this.startDelay + " TJ: " + this.tempoParaComer);
            Thread.sleep(this.startDelay); //delay proposital para causar maior aleatoriedade nos eventos
            //início das atividades a serem realizadas pelos clientes
            entrarEstabelecimento();
            procurarLugarESentar();
            sentar();
            consumirJantar();
            irEmbora();
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
        int nrThreads = 100;
        //números aleatórios para causar maior diversidade de eventos no programa
        Random random = new Random();
        int randomComer = 10000;
        int randomStart = 10000;

        //criação dos clientes e colocação na lista de clientes
        for(int i = 0; i < nrThreads; ++i) {
            clientes.add(new Thread(new Cliente(random.nextInt(randomComer), restaurante, random.nextInt(randomStart)), "Cliente " + i));
        }

        //inicia todas as threads
        for (Thread t: clientes) {
            t.start();
        }

        //todos os clientes jantaram, fim do programa
        System.out.println("Restaurante fechando...");
    }
}
