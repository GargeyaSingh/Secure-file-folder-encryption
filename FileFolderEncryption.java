import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.*;
import java.awt.*;


public class FileFolderEncryption {

    public static void zipFolder(File folder, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            zip(folder, folder.getName(), zos);
        }
    }

    public static void encryptFolder(File folder, int key) throws IOException {

        String zipFilePath = folder.getAbsolutePath() + "_encrypted.zip";
        zipFolder(folder, zipFilePath);

        encryptFile(new File(zipFilePath), key);

        // Delete the original unencrypted folder after encryption
        deleteFolder(folder);
    }

    public static void decryptFolder(File encryptedZipFile, int key) throws IOException {

        decryptFile(encryptedZipFile, key);

        String outputFolder = encryptedZipFile.getParentFile().getAbsolutePath();
        unzipFolder(encryptedZipFile, outputFolder);
    }

    private static void zip(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        byte[] buffer = new byte[4096];

        for (File file : files) {
            if (file.isDirectory()) {
                zip(file, parentFolder + "/" + file.getName(), zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));

                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
            }
        }
    }

    private static void unzipFolder(File zipFile, String outputFolder) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];

            while ((entry = zis.getNextEntry()) != null) {
                String filePath = outputFolder + File.separator + entry.getName();
                File outputFile = new File(filePath);

                if (entry.isDirectory()) {
                    if (!outputFile.exists() && !outputFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + filePath);
                    }
                } else {
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public static void encryptFile(File file, int key) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (data[i] ^ key);
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decryptFile(File file, int key) {
        encryptFile(file, key);
    }

    private static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        // Delete the folder itself
        folder.delete();
    }

    public static void main(String[] args) {
        System.out.println("This is testing");

        JFrame f = new JFrame();
        f.setTitle("File/Folder Encryption/Decryption");
        f.setSize(400, 400);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font = new Font("Roboto", Font.BOLD, 25);

        JButton fileButton = new JButton();
        fileButton.setText("Encrypt/Decrypt File");
        fileButton.setFont(font);

        JButton folderButton = new JButton();
        folderButton.setText("Encrypt/Decrypt Folder");
        folderButton.setFont(font);

        JTextField textField = new JTextField(10);
        textField.setFont(font);

        fileButton.addActionListener(e -> {
            System.out.println("File Button clicked");
            String text = textField.getText();
            int key = Integer.parseInt(text);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(null);

            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                if (selectedFile.isFile()) {
                    encryptFile(selectedFile, key);
                    JOptionPane.showMessageDialog(null, "File Encryption/Decryption Done");
                }
            }
        });

        folderButton.addActionListener(e -> {
            System.out.println("Folder Button clicked");
            String text = textField.getText();
            int key = Integer.parseInt(text);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(null);

            File selectedFolder = fileChooser.getSelectedFile();
            if (selectedFolder != null) {
                try {
                    if (selectedFolder.isDirectory()) {
                        encryptFolder(selectedFolder, key);
                        JOptionPane.showMessageDialog(null, "Folder Encryption Done");
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Folder Encryption Failed");
                }
            }
        });

        f.setLayout(new FlowLayout());

        f.add(fileButton);
        f.add(folderButton);
        f.add(textField);
        f.setVisible(true);
    }
}
