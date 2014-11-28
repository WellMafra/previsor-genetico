package org.dabliu.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.MonthDateFormat;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Month;

public class Main {

    private static final float MAX_VALUE = 10f;
    private static final float MIN_VALUE = 0.1f;
    private static final int MAX_SAMPLES = 12;
    private static final int QT_CHROMOSSOMES = 20;
    private static final int EPOCHS = 50;
    
    private static HashMap<Integer, Float> fitnesses = new HashMap<Integer, Float>();
    
    public static void main(String[] args) throws Exception {
        List<Float> initialValues = initValues();
        List<List<Float>> population = createInitialPopulation();
        for (int i = 0; i < EPOCHS; i++) {
            fitnesses = new HashMap<Integer, Float>();
            fitness(population, initialValues);
            population = crossover(sortFitness(), population);
        }
        Entry<Integer, Float> bestIndividual = sortFitness().entrySet().stream().findFirst().get();
        System.out.println("Index="+bestIndividual.getKey());
        System.out.println("Fitness="+bestIndividual.getValue());
        System.out.println(initialValues);
        System.out.println(population.get(bestIndividual.getKey()));
        
        createPrevisorGraph(population, bestIndividual);
        createOriginalGraph(initialValues);
    }

    private static void createPrevisorGraph(List<List<Float>> population,
            Entry<Integer, Float> bestIndividual) throws FileNotFoundException,
            IOException {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        
        List<Float> list = population.get(bestIndividual.getKey());
        for (int i = 0; i < 12; i++) {
            Float value = list.get(i);
            ds.addValue(value , "Valor", "Mes " + (i+1));
        }
        
        JFreeChart grafico = ChartFactory.createLineChart("Previsão de Clientes", "Quantidade de Cliente", "Meses", ds, PlotOrientation.VERTICAL, false, false, false);
        OutputStream out = new FileOutputStream("D:\\previsao.png");
        ChartUtilities.writeChartAsPNG(out, grafico, 1024, 768);
        out.close();
    }
    
    private static void createOriginalGraph(
            List<Float> initialValues) throws FileNotFoundException,
            IOException {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        
        for (int i = 0; i < 12; i++) {
            Float value = initialValues.get(i);
            ds.addValue(value , "Valor", "Mes " + (i+1));
        }
        
        JFreeChart grafico = ChartFactory.createLineChart("Previsão de Clientes", "Quantidade de Cliente", "Meses", ds, PlotOrientation.VERTICAL, false, false, false);
        OutputStream out = new FileOutputStream("D:\\original.png");
        ChartUtilities.writeChartAsPNG(out, grafico, 1024, 768);
        out.close();
    }

    private static List<List<Float>> createInitialPopulation() {
        List<List<Float>> population = new ArrayList<List<Float>>();
        for (int i = 0; i < QT_CHROMOSSOMES; i++) {
            population.add(initValues());
        }
        return population;
    }

    private static List<Float> initValues() {
        List<Float> result = new ArrayList<Float>();
        for (int i = 0; i < MAX_SAMPLES; i++) {
            float value = round(new Random().nextFloat() * (MAX_VALUE - MIN_VALUE) + MIN_VALUE);
            result.add(value);
        }
        return result;
    }
    
    private static void fitness(List<List<Float>> population, List<Float> initialValues) {
        float fitness;
        for (int i = 0; i < population.size(); i++) {
            fitness = 0f;
            for (int j = 0; j < MAX_SAMPLES; j++) {
                float populationValue = population.get(i).get(j).floatValue();
                float distinctValue = populationValue - initialValues.get(j).floatValue();
                
                double poweredValue = Math.pow(distinctValue, 2d);
                double squareRootedValue = Math.sqrt(poweredValue);
                fitness += squareRootedValue / MAX_SAMPLES;
            }
            fitnesses.put(Integer.valueOf(i), fitness);
        }
    }
    
    private static Map<Integer, Float> sortFitness() {
        Map<Integer, Float> treeMap = new TreeMap<Integer, Float>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (fitnesses.get(o1) > fitnesses.get(o2))
                    return 1;
                else
                    return -1;
            }
        });
        treeMap.putAll(fitnesses);
        return treeMap;
    }
    
    private static List<List<Float>> crossover(Map<Integer, Float> map, List<List<Float>> chromossomes) {
        Iterator<Entry<Integer, Float>> iterator = map.entrySet().iterator();
        
        List<List<Float>> result = new ArrayList<List<Float>>();
        while (iterator.hasNext()) {
            List<Float> sons1 = new ArrayList<Float>();
            List<Float> sons2 = new ArrayList<Float>();
            
            List<Float> dad1 = chromossomes.get(iterator.next().getKey());
            List<Float> dad2 = chromossomes.get(iterator.next().getKey());
            
            for (int i = 0; i < MAX_SAMPLES; i++) {
                float son1Dna = crossoverFlat(dad1.get(i), dad2.get(i));
                float son2Dna = crossoverFlat(dad1.get(i), dad2.get(i));
                
                sons1.add(son1Dna);
                sons2.add(son2Dna);
            }
            
            result.add(sons1);
            result.add(sons2);
        }
        return result;
    }

    private static float crossoverFlat(Float dad1Dna, Float dad2Dna) {
        if (dad1Dna >= dad2Dna) {
            return round(new Random().nextFloat() * (dad1Dna - dad2Dna) + dad2Dna);
        }
        return round(new Random().nextFloat() * (dad2Dna - dad1Dna) + dad1Dna);
    }

    private static float round(final float value) {
        BigDecimal bd = new BigDecimal(value);
        return bd.setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue();
    }
}