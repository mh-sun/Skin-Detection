package skinDetection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    static File[] allNormalImage =null;
    static File[] allMaskImage =null;
    static double th=2.5;
    static double aqquracy=0;

    static int counter=0;

    static String outputImageFolder="D:\\StudyMaterials\\DBMS_2\\output\\";

    public static void main(String[] args) {
        String normalImageFolder="D:\\StudyMaterials\\DBMS_2\\dataset\\";
        String maskImageFolder="D:\\StudyMaterials\\DBMS_2\\mask_dataset\\";

        File NIF=new File(normalImageFolder);
        allNormalImage=NIF.listFiles();
//        System.out.printf(allNormalImage[0].getName());

        File MIF=new File(maskImageFolder);
        allMaskImage=MIF.listFiles();
//        System.out.printf(allMaskImage[0].getName());
//
//        System.out.printf(Integer.toString(allNormalImage.length));
//        System.out.printf(Integer.toString(allMaskImage.length));

        int start=0,end=55;

        int fold=1;
        assert allMaskImage != null;
        for(; start<allMaskImage.length; start=end+1,end=start+55)
        {
            aqquracy=0;
            System.out.println("We are on "+fold++ +"th fold##############################################");
            double[][][] skin = new double[256][256][256];
            double[][][] nonSkin = new double[256][256][256];

            if(end>=allMaskImage.length) end=allMaskImage.length-1;

//            System.out.println(start+" "+end);
            trainData(start,end,skin,nonSkin);

            testData(start,end,skin,nonSkin);

            if(fold-1<10)
                System.out.println("Accuracy for "+(fold-1)+"th fold is: "+aqquracy/56+"%");
            else
                System.out.println("Accuracy for "+(fold-1)+"th fold is: "+aqquracy/51+"%");
        }
    }
    //Avoid start to end
    static void trainData(int start, int end, double[][][] skin, double[][][] nonSkin)
    {
//        System.out.println("Trainning////////////////////////////////////////////");
        for (int i=0;i<allMaskImage.length;i++)
        {
            if(i>=start && i<=end) continue;
//            System.out.println(allNormalImage[i].getPath()+"\n"+allMaskImage[i].getPath());
            BufferedImage normalImage = null;
            try {
                normalImage = ImageIO.read(allNormalImage[i]);
            } catch (IOException ignored){
            }

            BufferedImage maskImage = null;
            try {
                maskImage = ImageIO.read(allMaskImage[i]);
            } catch (IOException ignored){
            }

            assert normalImage != null;
            makeSkinAndNonSkinArray(normalImage,maskImage,skin,nonSkin);
        }
        makeProbabilityArray(skin,nonSkin);

    }

    private static void makeProbabilityArray(double[][][] skin, double[][][] nonSkin) {
        int skinSum=0,nonSkinSum=0;
        for(int i=0;i<256;i++)
        {
            for(int j=0;j<256;j++)
            {
                for(int k=0;k<256;k++)
                {
                    skinSum+=skin[i][j][k];
                    nonSkinSum+=nonSkin[i][j][k];
                }
            }
        }
        for(int i=0;i<256;i++)
        {
            for(int j=0;j<256;j++)
            {
                for(int k=0;k<256;k++)
                {
                    skin[i][j][k]/=skinSum;
                    nonSkin[i][j][k]/=nonSkinSum;
                }
            }
        }
    }

    static void makeSkinAndNonSkinArray(BufferedImage normalImage, BufferedImage maskImage, double[][][] skin, double[][][] nonSkin)
    {
        for(int i=0;i<normalImage.getWidth();i++)
        {
            for(int j=0;j<normalImage.getHeight();j++)
            {
                Color normalImagePixel=new Color(normalImage.getRGB(i,j));
                Color maskImagePixel=new Color(maskImage.getRGB(i,j));

                int r=normalImagePixel.getRed(),g=normalImagePixel.getGreen(),b=normalImagePixel.getBlue();

//                System.out.println("Red:"+r+"\nGreen:"+g+"\nBlue:"+b+"\n\n");
                if(maskImagePixel.getBlue()>=250 && maskImagePixel.getRed()>=250 && maskImagePixel.getGreen()>=250)
                {
                    nonSkin[r][g][b]++;
                }
                else{
                    skin[r][g][b]++;
                }
            }
        }
    }
    //test start to end
    static void testData(int start, int end,double [][][]skin,double [][][]nonSkin)
    {
        int tp=0,tn=0,fp=0,fn=0;
//        System.out.println("Testing////////////////////////////////////////////");
        for(int i=start;i<=end ;i++)
        {
            System.out.println("Testing image number :...................................."+i);
//            System.out.println(allNormalImage[i].getPath()+"\n"+allMaskImage[i].getPath());
            BufferedImage NormalImage = null;
            try {
                NormalImage = ImageIO.read(allNormalImage[i]);
            } catch (IOException ignored){
            }
            BufferedImage maskImage = null;
            try {
                maskImage = ImageIO.read(allMaskImage[i]);
            } catch (IOException ignored){
            }
            assert NormalImage != null;
//            BufferedImage outputImage=new BufferedImage(NormalImage.getWidth(),NormalImage.getHeight(),BufferedImage.TYPE_INT_RGB);

            makeOutputImage(maskImage,NormalImage,skin,nonSkin,tp,tn,fp,fn,i);

//            File outputfile = new File(outputImageFolder+counter++ +".jpg");
//            try {
//                ImageIO.write(outputImage, "jpg", outputfile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private static void makeOutputImage(BufferedImage maskImage, BufferedImage normalImage, double[][][] skin, double[][][] nonSkin, int tp, int tn, int fp, int fn, int imageNumber) {
        for(int i=0;i<normalImage.getWidth();i++)
        {
            for(int j=0;j<normalImage.getHeight();j++)
            {
                int RGB=normalImage.getRGB(i,j);
                Color c=new Color(RGB);
                int r=c.getRed(),g=c.getGreen(),b=c.getBlue();

                //System.out.println("Red:"+r+"\nGreen:"+g+"\nBlue:"+b+"\n\n");
                if(nonSkin[r][g][b]==0)
                {
                    if(skin[r][g][b]!=0){
                        if(maskImage.getRGB(i,j)==Color.white.getRGB()) fp++;
                        else tp++;

//                        outputImage.setRGB(i,j,RGB);
                        continue;
                    }
                    else {
                        if(maskImage.getRGB(i,j)==Color.white.getRGB()) tn++;
                        else fn++;

//                        outputImage.setRGB(i,j,Color.white.getRGB());
                        continue;
                    }
                }
                double ratio=skin[r][g][b]/nonSkin[r][g][b];
                //System.out.println(ratio);
                if(ratio>th){

                    if(maskImage.getRGB(i,j)==Color.white.getRGB()) fp++;
                    else tp++;

//                    outputImage.setRGB(i,j,RGB);
                }
                else {
                    if(maskImage.getRGB(i,j)==Color.white.getRGB()) tn++;
                    else fn++;

//                    outputImage.setRGB(i,j,Color.white.getRGB());
                }
            }
        }
        //System.out.println("Accuracy for "+imageNumber+"th photo is: "+(tp+tn)*100/(tp+tn+fp+fn)+"%");
        aqquracy+=((tp+tn)*100.0/(tp+tn+fp+fn));
    }
}
