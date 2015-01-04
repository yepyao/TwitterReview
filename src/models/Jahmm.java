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

	double[][] scale;
	
	LinkedList<LinkedList<ObservationVector>> sequences;

	public Jahmm(int states, int dim) {
		this.states = states;
		this.dim = dim;

		double[] pi = new double[states];
		for (int i = 0; i < states; i++)
			pi[i] = 1. / states;
		double[][] a = new double[states][states];
		for (int i = 0; i < states; i++)
			for (int j = 0; j < states; j++)
				a[i][j] = 1. / states;

		LinkedList<OpdfMultiGaussian> opdfs = new LinkedList<OpdfMultiGaussian>();
		double[] mean = new double[dim];
		double[][] covariance = new double[dim][dim];
		for (int i = 0; i < dim; i++)
			mean[i] = Math.random();
		for (int i = 0; i < dim; i++)
			for (int j = 0; j < dim; j++)
				covariance[i][j] = (i == j) ? 1 : 0;
		opdfs.add(new OpdfMultiGaussian(mean, covariance));

		for (int i = 0; i < dim; i++)
			mean[i] = Math.random();
		for (int i = 0; i < dim; i++)
			for (int j = 0; j < dim; j++)
				covariance[i][j] = (i == j) ? 1 : 0;
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

	public LinkedList<LinkedList<Integer>> getStateList() {
		LinkedList<LinkedList<Integer>> lists = new LinkedList<LinkedList<Integer>>();
		for (LinkedList<ObservationVector> sequence : sequences) {
			int length = sequence.size();
			int[][] pre = new int[length][states];
			double[][] pro = new double[length][states];
			ObservationVector o = sequence.get(0);
			for (int j = 0; j < states; j++)
				pro[0][j] = learntHmm.getOpdf(j).probability(o);
			for (int i = 1; i < length; i++) {
				o = sequence.get(i);
				for (int j = 0; j < states; j++) {
					double max = -1;
					for (int k = 0; k < states; k++) {
						if (pro[i - 1][k] * learntHmm.getAij(k, j) > max) {
							max = pro[i - 1][k] * learntHmm.getAij(k, j);
							pre[i][j] = k;
						}
					}
					pro[i][j] = max * learntHmm.getOpdf(j).probability(o);
				}
			}
			
			boolean state_swap = ((OpdfMultiGaussian)learntHmm.getOpdf(0)).mean()[0]> ((OpdfMultiGaussian)learntHmm.getOpdf(1)).mean()[0];
			double max = -1;
			int ls = 0;
			for (int j = 0; j < states; j++) {
				if (pro[length - 1][j] > max) {
					max = pro[length - 1][j];
					ls = j;
				}
			}
			LinkedList<Integer> list = new LinkedList<Integer>();
			int p = length;
			while (p > 0) {
				p--;
				list.addFirst((state_swap)?(1-ls):ls);
				ls = pre[p][ls];
			}
			lists.add(list);
		}
		return lists;
	}

	public void scale(double[][] scale) {
		this.scale = scale;
		for(LinkedList<ObservationVector> sequence: sequences){
			for(ObservationVector o : sequence){
				double[] v = o.values();
				for(int i=0;i<dim;i++){
					v[i] = (v[i]-scale[i][0])/(scale[i][1]-scale[i][0]);
				}
				o.setValue(v);
			}
		}
		
	}
}
