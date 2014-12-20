package models;

import java.util.LinkedList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchScaledLearner;

public class Jahmm {
	int dim;
	int states;
	Hmm<ObservationVector> learntHmm;
	Hmm<ObservationVector> init_hmm;
	BaumWelchLearner bwl;

	LinkedList<LinkedList<ObservationVector>> sequences;

	public Jahmm(int states, int dim) {
		this.states = states;
		this.dim = dim;

		double[] pi = new double[states];
		for(int i=0;i<states;i++)
			pi[i] = 1./states;
		double[][] a = new double[states][states];
		for(int i=0;i<states;i++)
			for(int j=0;j<states;j++)
				a[i][j] = 1./states;
		
		LinkedList<OpdfMultiGaussian> opdfs = new LinkedList<OpdfMultiGaussian>();
		double[] mean = new double[dim];
		double[][] covariance = new double[dim][dim];
		for(int i=0;i<dim;i++)
			mean[i] = Math.random();
		for(int i=0;i<dim;i++)
			for(int j=0;j<dim;j++)
				covariance[i][j] = (i==j)?1:0;
		opdfs.add(new OpdfMultiGaussian(mean, covariance));
		
		for(int i=0;i<dim;i++)
			mean[i] = Math.random();
		for(int i=0;i<dim;i++)
			for(int j=0;j<dim;j++)
				covariance[i][j] = (i==j)?1:0;
		opdfs.add(new OpdfMultiGaussian(mean, covariance));
		
		
		init_hmm = new Hmm<ObservationVector>(pi, a, opdfs);
		bwl = new BaumWelchLearner();
		sequences = new LinkedList<LinkedList<ObservationVector>>();
	}

	public void addSequence(LinkedList<double[]> sequence) {
		// System.out.println("sequence length: "+sequence.size());
		LinkedList<ObservationVector> s = new LinkedList<ObservationVector>();
		for (double[] vector : sequence) {
			if (vector.length != dim) {
				System.err.print("error in dim num!");
				System.exit(1);
			}
			ObservationVector o = new ObservationVector(vector);
			s.add(o);
		}
		if (s.size() > 1)
			sequences.add(s);
	}

	public void learn(int round) {
		System.out.println("sequences size: " + sequences.size());
		learntHmm = init_hmm;
		System.out.println(learntHmm.toString());
		bwl.setNbIterations(1);
		for (int i = 0; i < round; i++) {
			learntHmm = bwl.learn(learntHmm, sequences);
			System.out.println("Jahmm round " + i + " OK!");
			System.out.println(learntHmm.toString());
		}

	}
}
