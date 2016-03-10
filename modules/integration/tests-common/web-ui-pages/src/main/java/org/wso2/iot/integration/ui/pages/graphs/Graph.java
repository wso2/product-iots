package org.wso2.iot.integration.ui.pages.graphs;

/**
 * Class to store graph data
 */
public class Graph {
    private String graphId;
    private String yAxis;
    private String xAxis;
    private String legend;

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public String getyAxis() {
        return yAxis;
    }

    public void setyAxis(String yAxis) {
        this.yAxis = yAxis;
    }

    public String getxAxis() {
        return xAxis;
    }

    public void setxAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    @Override
    public String toString(){
        return String.format("The graph for graph id : %s, X - axis : %s, Y - axis : %s, legend : %s ", graphId,
                             xAxis, yAxis, legend);
    }
}
