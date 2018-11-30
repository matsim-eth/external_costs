package ch.ethz.matsim.external_costs.dispersion;

public class GaussianDispersionModel implements DispersionModel {

    private double sigma;

    public GaussianDispersionModel(double sigma) {
        this.sigma = sigma;
    }

    @Override
    public double calculate(double r) {
        double C = 1 / (2*Math.PI*Math.pow(sigma,2));
        return C * Math.exp(- Math.pow(r,2) / (2 * Math.pow(sigma,2)));
    }
}
