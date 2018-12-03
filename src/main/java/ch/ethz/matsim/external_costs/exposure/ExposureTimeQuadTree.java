package ch.ethz.matsim.external_costs.exposure;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

import java.util.Collection;
import java.util.HashSet;

public class ExposureTimeQuadTree {

    final private Scenario scenario;
    private final int timeBinSize;
    private final int noTimeBins;
    private final double cellSize;
    private final int noCells;
    private QuadTree<ExposureItem> quadTree;
    private double[] sumOverAllCellsPerTimeBin;
    final private double exposureDistance;

    public ExposureTimeQuadTree(Scenario scenario, int timeBinSize, int noTimeBins, double cellSize, double exposureDistance){
        this.scenario = scenario;
        this.timeBinSize = timeBinSize;
        this.noTimeBins = noTimeBins;
        this.cellSize = cellSize;
        this.sumOverAllCellsPerTimeBin = new double[noTimeBins];
        this.exposureDistance = exposureDistance;

        // initiate quad tree
        double[] boundingBox = NetworkUtils.getBoundingBox(scenario.getNetwork().getNodes().values());
        this.quadTree = new QuadTree<>(boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]);

        int noXCells = (int) Math.floor((boundingBox[2] - boundingBox[0]) / cellSize) + 1;
        int noYCells = (int) Math.floor((boundingBox[3] - boundingBox[1]) / cellSize) + 1;
        this.noCells = noXCells * noYCells;
    }

    public void add(Coord coord, int timeBin, double exposure) {
        this.quadTree.getClosest(coord.getX(), coord.getY()).getExposureTimes()[timeBin] += exposure;
        this.sumOverAllCellsPerTimeBin[timeBin] += exposure;
    }

    public void addNewExposureItemAt(Coord coord) {
        double xMin = quadTree.getMinEasting();
        double yMin = quadTree.getMinNorthing();
        int nx = (int) Math.round( (coord.getX() - xMin) / cellSize);
        int ny = (int) Math.round( (coord.getY() - yMin) / cellSize);

        double x = xMin + nx*cellSize;
        double y = yMin + ny*cellSize;

        this.quadTree.put(x,y, new ExposureItem(new double[noTimeBins], new Coord(x,y)));
    }

    public boolean isContaining(Coord coord) {
        double minX = coord.getX() - cellSize/2.0;
        double minY = coord.getY() - cellSize/2.0;
        double maxX = coord.getX() + cellSize/2.0;
        double maxY = coord.getY() + cellSize/2.0;

        Collection<ExposureItem> collection = new HashSet<>();
        quadTree.getRectangle(minX, minY, maxX, maxY, collection);

        return !collection.isEmpty();
    }

    public double getExposureTimeFactorFor(Coord coord, int timeBin) {
        Collection<ExposureItem> exposureItems = this.quadTree.getDisk(coord.getX(), coord.getY(), this.exposureDistance);

        // get average exposure for all cells in time bin
        double averageExposure = this.sumOverAllCellsPerTimeBin[timeBin] / this.noCells;

        // if average is zero, then factor is zero (as no exposures were recorded in that timebin)
        if (averageExposure == 0.0) {
            return 0.0;
        }

        // otherwise, calculate the sum of exposures within timebin for all cells within neighborhood of coordinate
        double sumOfExposures = 0.0;
        for (ExposureItem exposureItem : exposureItems) {
            sumOfExposures += exposureItem.getExposureTimes()[timeBin];
        }

        // return relative factor
        return sumOfExposures / averageExposure;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public QuadTree<ExposureItem> getDuration() {
        return quadTree;
    }

    public int getTimeBinSize() {
        return timeBinSize;
    }

    public int getNoTimeBins() {
        return noTimeBins;
    }

    public double getCellSize() {
        return cellSize;
    }

    public int getNoCells() {
        return noCells;
    }

    public QuadTree<ExposureItem> getQuadTree() {
        return quadTree;
    }
}
