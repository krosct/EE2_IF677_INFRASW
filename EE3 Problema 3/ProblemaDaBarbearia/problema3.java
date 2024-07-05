import java.util.concurrent.Semaphore;

public class problema3 {
    //Classe da barbearia ela vai ter duas funções importantes "EntrarNaBarbearia" e "cortarCabelo"
    public static  class Barbearia{
        public int assentos;
        public int assentosOcupados = 0;
        public Barbeiro barbeiro;
        // Existem 3 semáforos "Cadeiras" controla a quantidade de clientes sentados na cadeira
        // "Porta" controla a quantidade de clientes que passam pela porta no começo
        // "Corte" controla a quantidade de clientes que cortam o cabelo
        private static  Semaphore semaphoreCadeiras;
        private static final Semaphore semaphorePorta = new Semaphore(1);
        private static final Semaphore semaphoreCorte = new Semaphore(1);


        //Classe barbeiro recebe a quantidade de assentos disponíveis e um barbeiro
        public Barbearia(int assentos,Barbeiro barbeiro){
            this.assentos = assentos;
            this.barbeiro = barbeiro;
            semaphoreCadeiras = new Semaphore(assentos);
        }

        //Aqui está a função principal ela define o que acontece com o cliente ao entrar na barbearia
        public void EntrarNaBarbearia(String nome) throws InterruptedException {
            //Já de início eu uso o semáforo da porta para permitir que um por vez verifique se o barbeiro está trabalhando ou está descansando
            semaphorePorta.acquire();
            if (barbeiro.trabalhando){
                //Caso o barbeiro esteja trabalhando é verificado se todas as cadeiras estão sendo ocupadas
                System.out.println(nome + " Entrando a Barbearia e vendo que tem " +assentos+" mas "+assentosOcupados+" estão ocupados");
                if (semaphoreCadeiras.tryAcquire()){
                    //Caso tenha cadeiras vazias ele vai pra um assento e entra na fila pra cortar o cabelo
                    Thread.sleep(1000);
                    assentosOcupados++;
                    System.out.println(nome + " se sentou  para esperar e agora temos  " + assentosOcupados + " ocupados");
                    semaphorePorta.release();
                    semaphoreCorte.acquire();
                    //Após chegar sua vez ele libera o assento que ocupava e executa a função de corte
                    assentosOcupados--;
                    this.cortarCabelo(nome);
                    semaphoreCorte.release();


                }
                else{
                    //Caso todos os assentos estejam ocupados ele vai embora
                    System.out.println(nome + " foi embora pois estava lotado !");
                    semaphorePorta.release();

                }

            }
            else{
                //Caso o barbeiro esteja descansando é chamada a função "acordarBarbeiro" e em seguida a função "cortarCabelo
                this.barbeiro.acordarBarbeiro(nome);
                semaphorePorta.release();
                semaphoreCorte.acquire();
                this.cortarCabelo(nome);
                semaphoreCorte.release();

            }
        }

        //De forma simples a função simula um tempo de corte do cabelo
        public void cortarCabelo(String nome) throws InterruptedException {
            System.out.println(nome + " está cortando o cabelo!...");
            Thread.sleep(3000);
            System.out.println("-------------Finalizado--------------");

            //Ao final de todo corte o barbeiro verifica se tem alguém sentado ainda, se não tiver ele vai descansar
            if (this.assentosOcupados == 0){
                this.barbeiro.descansarBarbeiro();
            }
        }
    }

    //A classe cliente recebe a barbearia que ele vai cortar o cabelo e o nome do cliente
    //Quando da o start ela executa a função "EntrarNaBarbearia"
    public static class Cliente implements Runnable{
        public String nome;
        public Barbearia barbearia;

        public Cliente(String nome, Barbearia barbearia){
            this.nome = nome;
            this.barbearia = barbearia;
        }

        public void run(){
            try {
                barbearia.EntrarNaBarbearia(nome);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //A classe barbeiro trata do status do barbeiro
    //O Barbeiro sempre chega na barbearia e vai dar um cochilo e só acorda quando um cliente acorda ele
    public static class Barbeiro{
        public boolean trabalhando = false;

        public void acordarBarbeiro(String nome){
            System.out.println("--->"+nome+" acordou o barberiro");
            trabalhando = true;
        };
        public void descansarBarbeiro(){
            System.out.println("---->Ninguém por perto vou tirar um cochilinho");
            trabalhando = false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Barbeiro barbeiro = new Barbeiro();
        Barbearia barbearia = new Barbearia(1,barbeiro);
        Cliente cliente1 = new Cliente("Jefersson", barbearia);
        Cliente cliente2 = new Cliente("JohnJohn", barbearia);
        Cliente cliente3 = new Cliente("Jack", barbearia);
        Cliente cliente4 = new Cliente("Jonas",barbearia);
        Thread thread1 = new Thread(cliente1);
        thread1.start();
        Thread thread2 = new Thread(cliente2);
        thread2.start();
        Thread thread3 = new Thread(cliente3);
        thread3.start();
        //Nesse ponto é simulado o tempo de espera de 20s até o próximo cliente entrar o que da o tempo para que o
        //barbeiro possa descansar
        Thread.sleep(20000);
        Thread thread4 = new Thread(cliente4);
        thread4.start();


    }

}