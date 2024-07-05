import java.util.Random;

public class Mainsem {
    //Classe que representa a ponte
    public static class Ponte{
        private int qtdCarrosDireita;
        private int qtdCarrosEsquerda;


        public  Ponte(int qtdCarrosDireita, int qtdCarrosEsquerda){
            this.qtdCarrosDireita = qtdCarrosDireita;
            this.qtdCarrosEsquerda = qtdCarrosEsquerda;
        }

            public void atravessarPonte(String ladoDoCarro) throws  InterruptedException{
            System.out.println("----Carro da "+ladoDoCarro+" quer passar pela ponte");


            System.out.println("Carro da "+ladoDoCarro+" passando......");
            //Caso o carro seja da esquerda ele decrementa a fila de carros da esquerda se não for ele decrementa da direita
            if(ladoDoCarro.contains("Esquerda")){
                this.qtdCarrosEsquerda -= 1;
            }
            else{
                this.qtdCarrosDireita -= 1;
            }
            System.out.println(">>>>>>Carro da "+ladoDoCarro+" passou pela ponte");

        }
    }

    //Classe que simboliza a Thread da travessia da ponte
    public static  class Travessia implements Runnable{
        private Ponte ponte;
        private String ladoDoCarro;

        public Travessia(Ponte ponte, String ladoDoCarro){
            this.ponte = ponte;
            this.ladoDoCarro = ladoDoCarro;
        }

        public void run(){
            try{
                ponte.atravessarPonte(ladoDoCarro);
            } catch (InterruptedException e){
                throw  new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {

        Random random = new Random();

        //Aqui ele gera um número aleatório de carros pra cada lado da ponte
        int carrosDaEsquerda = random.nextInt(5);
        int carrosDaDireita =  random.nextInt(5);
        int totalDeCarros = carrosDaDireita+carrosDaEsquerda;

        Ponte ponte1 = new Ponte(carrosDaDireita,carrosDaEsquerda);

        System.out.println("Carros na fila da esquerda: "+ponte1.qtdCarrosEsquerda);
        System.out.println("Carros na fila da direita: "+ponte1.qtdCarrosDireita);

        //Como é algo aleatório, decidimos colocar os objetos em arrays tanto os carros que desejam fazer a travessia, quanto as Threads
        Travessia[] carrosNaPonte = new Travessia[totalDeCarros];
        Thread[] threads =  new Thread[totalDeCarros];

        //Aqui é criado os carros da direita e em seguida os carros da esquerda
        for (int i = 0; i < carrosDaDireita; i++) {
            String valor = String.valueOf(i+1);
            Travessia carro = new Travessia(ponte1, "Direita "+valor);
            carrosNaPonte[i] = carro;
        }

        for (int i = carrosDaDireita; i < totalDeCarros; i++) {
            String valor = String.valueOf(i+1);
            Travessia carro = new Travessia(ponte1, "Esquerda "+valor);
            carrosNaPonte[i] = carro;
        }

        //Aqui é criado a Thread dos carros e iniciada e em seguida o join das threads
        for (int i = 0; i < totalDeCarros; i++) {
            Thread t = new Thread(carrosNaPonte[i]);
            threads[i] = t;
            threads[i].start();
        }

        for (int i = 0; i < totalDeCarros; i++) {
            threads[i].join();
        }



    }
}