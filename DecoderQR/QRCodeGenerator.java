import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator extends JFrame implements ActionListener {
    // Generator components
    private JTextField textField;
    private JButton generateButton;
    private JLabel generatorMessageLabel;
    private JLabel qrCodeImageLabel;
    private final String OUTPUT_FILE = "QRCode.png";

    // Decoder components
    private JButton selectFileButton;
    private JLabel decoderImageLabel;
    private JTextArea decodedTextArea;
    private JLabel decoderMessageLabel;

    public QRCodeGenerator() {
        // Window setup
        setTitle("QR Code Generator & Decoder");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with split layout
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  //set border id the fution of jcomponent class

        // Create generator and decoder panels
        JPanel generatorPanel = createGeneratorPanel();
        JPanel decoderPanel = createDecoderPanel();

        // Add panels to main panel
        mainPanel.add(generatorPanel);
        mainPanel.add(decoderPanel);

        // Add main panel to frame
        add(mainPanel);
    }

    private JPanel createGeneratorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "QR Code Generator", 
                TitledBorder.CENTER, 
                TitledBorder.TOP));

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel label = new JLabel("Enter text or URL:");
        textField = new JTextField(20);
        generateButton = new JButton("Generate");
        generateButton.setPreferredSize(new Dimension(200, 40)); 
        generateButton.setMargin(new Insets(10, 0, 10, 0));
        generateButton.addActionListener(this);

        inputPanel.add(label);
        inputPanel.add(textField);
        inputPanel.add(generateButton);

        // QR code display panel
        JPanel displayPanel = new JPanel(new BorderLayout());
        qrCodeImageLabel = new JLabel();
        qrCodeImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrCodeImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        qrCodeImageLabel.setPreferredSize(new Dimension(300, 300));

        // Message label
        generatorMessageLabel = new JLabel("Enter text and click Generate");
        generatorMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        displayPanel.add(qrCodeImageLabel, BorderLayout.CENTER);
        displayPanel.add(generatorMessageLabel, BorderLayout.SOUTH);

        // Add components to main panel
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(displayPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDecoderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "QR Code Decoder", 
                TitledBorder.CENTER, 
                TitledBorder.TOP));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        selectFileButton = new JButton("Select QR Code Image");
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAndDecodeQRCode();
            }
        });

        buttonPanel.add(selectFileButton);

        // Image display panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        decoderImageLabel = new JLabel();
        decoderImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        decoderImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        decoderImageLabel.setPreferredSize(new Dimension(200, 200));

        // Decoded text panel
        JPanel textPanel = new JPanel(new BorderLayout(10, 10));
        JLabel decodedLabel = new JLabel("Decoded Content:");
        decodedTextArea = new JTextArea(5, 20);
        decodedTextArea.setEditable(false);
        decodedTextArea.setLineWrap(true);
        decodedTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(decodedTextArea);

        textPanel.add(decodedLabel, BorderLayout.NORTH);
        textPanel.add(scrollPane, BorderLayout.CENTER);

        // Message label
        decoderMessageLabel = new JLabel("Select a QR code image to decode");
        decoderMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Combine image and text panels
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(decoderImageLabel, BorderLayout.CENTER);
        contentPanel.add(textPanel, BorderLayout.SOUTH);

        // Add components to main panel
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(decoderMessageLabel, BorderLayout.SOUTH);

        return panel;
    }


    //Above all is about the GUI using AWT and SWING THE main Logic is Below 

    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == generateButton) {
            generateQRCode();
        }
    }

    // This method generates a QR code from the text input by the user.
private void generateQRCode() {

    // "UTF-8" stands for Unicode Transformation Format - 8-bit.
    
    // Get the text from the input field and trim extra spaces
    String text = textField.getText().trim();

    // If the text field is empty, show an error message and exit
    if (text.isEmpty()) {
        generatorMessageLabel.setText("Please enter text or URL");
        generatorMessageLabel.setForeground(Color.RED); // Set text color to red
        return; // Stop further execution
    }

    try {
        // Create a map of encoding hints for the QR code
        Map<EncodeHintType, Object> hints = new HashMap<>();

        // Set high error correction level (allows QR to work even if damaged)
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        // Set character encoding to UTF-8 (supports emojis, special characters)
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // Set margin around QR code (white border area)
        hints.put(EncodeHintType.MARGIN, 1);

        // Create a BitMatrix from the input text
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
            text,                       // Input text or URL
            BarcodeFormat.QR_CODE,     // Type of barcode (QR code)
            500,                       // Width of the QR code
            500,                       // Height of the QR code
            hints                      // Encoding hints map
        );


        // Convert the QR matrix to an image that can be shown in the GUI
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Scale the image and add it to the JLabel in the GUI
        ImageIcon icon = new ImageIcon(qrImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH));
        qrCodeImageLabel.setIcon(icon); // Show QR code image in label

        // Show a success message in dark green
        generatorMessageLabel.setText("QR Code generated successfully: " + OUTPUT_FILE);
        generatorMessageLabel.setForeground(new Color(0, 128, 0)); // Dark green color

    } catch (Exception ex) {
        // If any error occurs, show it in the GUI and popup
        generatorMessageLabel.setText("Error: " + ex.getMessage());
        generatorMessageLabel.setForeground(Color.RED);

        JOptionPane.showMessageDialog(
            this, 
            "Error generating QR code: " + ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }
}
private void selectAndDecodeQRCode() {
    // Create a file chooser dialog to select image file
    JFileChooser fileChooser = new JFileChooser();

    // Set the title of the file chooser dialog window
    fileChooser.setDialogTitle("Select QR Code Image");

    // Allow only image files (jpg, jpeg, png, gif, bmp) to be selected
    fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));

    // Show the file chooser dialog and store user's action (Approve/Cancel)
    int result = fileChooser.showOpenDialog(this);

    // If user selects a file and clicks "Open"
    if (result == JFileChooser.APPROVE_OPTION) {

        // Get the selected file
        File selectedFile = fileChooser.getSelectedFile();

        try {
            // Read the image file into a BufferedImage object
            BufferedImage image = ImageIO.read(selectedFile);

            // If image could not be read (null), throw error
            if (image == null) {
                throw new Exception("Could not read image file");
            }

            // Resize and display the selected image in the GUI
            ImageIcon icon = new ImageIcon(image.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
            decoderImageLabel.setIcon(icon); // `decoderImageLabel` is a JLabel in your GUI

            // Call a method to decode the QR code from the image THIS IS THE MAIN FUNCTION OF DECODE

            String decodedText = decodeQRCode(image);

            // Display the decoded text inside a text area
            decodedTextArea.setText(decodedText);

            // Check if the decoded text is a URL (starts with http:// or https://)
            if (decodedText.startsWith("http://") || decodedText.startsWith("https://")) {
                decoderMessageLabel.setText("URL detected: " + decodedText);
            } else {
                decoderMessageLabel.setText("QR Code decoded successfully");
            }

            // Set the message text color to dark green for success
            decoderMessageLabel.setForeground(new Color(0, 128, 0));

        } catch (NotFoundException nfe) {
            // If no QR code is found in the image
            decodedTextArea.setText(""); // Clear the text area
            decoderMessageLabel.setText("No QR code found in the image");
            decoderMessageLabel.setForeground(Color.RED); // Show error in red color

        } catch (Exception ex) {
            // Handle other unexpected errors
            decodedTextArea.setText(""); // Clear the text area
            decoderMessageLabel.setText("Error: " + ex.getMessage());
            decoderMessageLabel.setForeground(Color.RED); // Show error in red color

            // Show a pop-up error dialog
            JOptionPane.showMessageDialog(this, 
                "Error decoding QR code: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

  private String decodeQRCode(BufferedImage image) throws Exception {
    // Convert the image into a binary bitmap (format needed for decoding QR codes)
    // BufferedImageLuminanceSource extracts brightness values from the image
    // HybridBinarizer helps convert it into black and white pixels (binary)
    BinaryBitmap binaryBitmap = new BinaryBitmap(
            new HybridBinarizer(new BufferedImageLuminanceSource(image)));

    // Create a map of hints (optional settings) to help the decoder
    Map<DecodeHintType, Object> hints = new HashMap<>();

    // TRY_HARDER tells the decoder to use more effort to find a QR code (slow but accurate)
    hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

    // We are only looking for QR_CODE format (not barcodes or others)
    hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));

    // Use MultiFormatReader from ZXing library to decode the binary bitmap using the hints
    Result result = new MultiFormatReader().decode(binaryBitmap, hints);

    // Return the decoded text from the QR code
    return result.getText();
}


public static void main(String[] args) {
    try {
        // Set the application's look and feel to match the system's native theme (e.g., Windows, macOS, Linux)
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
        // If setting the look and feel fails, print the stack trace for debugging
        e.printStackTrace();
    }

    // Start the Swing application in the Event Dispatch Thread (EDT) for thread safety
    SwingUtilities.invokeLater(() -> {
        // Create an instance of the QRCodeGenerator class
        QRCodeGenerator generator = new QRCodeGenerator();
        
        // Make the application window visible
        generator.setVisible(true);
    });
}

}




