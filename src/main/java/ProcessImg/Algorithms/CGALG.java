package ProcessImg.Algorithms;

import org.jblas.DoubleMatrix;

import java.math.BigDecimal;

public class CGALG {
    /**
     * Fornece o erro entre as duas matrizes.
     * @param newR
     * @param r
     * @return
     */
    protected static double error(DoubleMatrix newR, DoubleMatrix r){
        double newR_norm = newR.norm2();
        double R_norm = r.norm2();
        double error = newR_norm - R_norm;

        if(error>=0)
            return error;
        else
            return error * -1;
    }

}
