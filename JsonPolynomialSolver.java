import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class JsonPolynomialSolver {
    public static void main(String[] args) {
        try {
            // Read JSON file
            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Student\\Desktop\\pw\\input.json"));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();

            // Convert JSON to String and remove spaces
            String jsonString = jsonContent.toString().replaceAll("\\s+", "");

            // Extract "keys" section
            int nStart = jsonString.indexOf("\"n\":") + 4;
            int nEnd = jsonString.indexOf(",", nStart);
            int n = Integer.parseInt(jsonString.substring(nStart, nEnd));

            int kStart = jsonString.indexOf("\"k\":") + 4;
            int kEnd = jsonString.indexOf("}", kStart);
            int k = Integer.parseInt(jsonString.substring(kStart, kEnd));

            // Store results in dictionary (Map)
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("Degree (n)", n);
            output.put("Roots count (k)", k);

            // Extract all values and bases dynamically
            List<Map<String, Object>> rootList = new ArrayList<>();
            List<Long> roots = new ArrayList<>();
            int index = jsonString.indexOf("\"1\":");

            while (index != -1) {
                int keyEnd = jsonString.indexOf(":", index);
                int baseStart = jsonString.indexOf("\"base\":\"", keyEnd) + 8;
                int baseEnd = jsonString.indexOf("\"", baseStart);
                int base = Integer.parseInt(jsonString.substring(baseStart, baseEnd));

                int valueStart = jsonString.indexOf("\"value\":\"", baseEnd) + 9;
                int valueEnd = jsonString.indexOf("\"", valueStart);
                String valueStr = jsonString.substring(valueStart, valueEnd);

                // Convert to base 10
                long root = parseBaseValue(valueStr, base);
                roots.add(root);

                // Store in dictionary format
                Map<String, Object> rootEntry = new LinkedHashMap<>();
                rootEntry.put("Base", base);
                rootEntry.put("Value", valueStr);
                rootEntry.put("Converted", root);
                rootList.add(rootEntry);

                index = jsonString.indexOf("\"", valueEnd + 1);
            }

            output.put("Roots", rootList);

            // Construct Vandermonde matrix and solve for coefficients
            double[][] A = new double[k][k];  // Vandermonde matrix
            double[] b = new double[k];       // Right-hand side of the system

            for (int i = 0; i < k; i++) {
                long root = roots.get(i);
                double power = 1.0;
                for (int j = k - 1; j >= 0; j--) {
                    A[i][j] = power;
                    power *= root;
                }
                b[i] = 0;  // Since f(root) = 0 for all roots
            }
            b[k - 1] = 1; // Ensuring the highest coefficient is nonzero

            // Solve using Gaussian elimination
            double[] coefficients = gaussianElimination(A, b);
            double constantTerm = coefficients[k - 1];

            // Store final result in dictionary
            output.put("Constant Term (c)", constantTerm);

            // Print the final dictionary output
            System.out.println(output);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to safely parse base-converted values
    public static long parseBaseValue(String value, int base) {
        // Ensure the string contains only valid characters for the given base
        String validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(0, base);
    
        for (char c : value.toUpperCase().toCharArray()) {
            if (validChars.indexOf(c) == -1) {
                throw new IllegalArgumentException("Invalid digit '" + c + "' for base " + base);
            }
        }
    
        try {
            return new java.math.BigInteger(value, base).longValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse value '" + value + "' in base " + base, e);
        }
    }
    

    // Gaussian elimination to solve Ax = b
    public static double[] gaussianElimination(double[][] A, double[] b) {
        int n = b.length;

        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(A[k][i]) > Math.abs(A[maxRow][i])) {
                    maxRow = k;
                }
            }

            // Swap maximum row with current row
            double[] temp = A[i];
            A[i] = A[maxRow];
            A[maxRow] = temp;

            double t = b[i];
            b[i] = b[maxRow];
            b[maxRow] = t;

            // Make the diagonal 1
            for (int k = i + 1; k < n; k++) {
                double factor = A[k][i] / A[i][i];
                for (int j = i; j < n; j++) {
                    A[k][j] -= factor * A[i][j];
                }
                b[k] -= factor * b[i];
            }
        }

        // Solve for x
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = b[i] / A[i][i];
            for (int k = 0; k < i; k++) {
                b[k] -= A[k][i] * x[i];
            }
        }
        return x;
    }
}
