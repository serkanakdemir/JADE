package Guis;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import Agents.GuiAgent;
import Ontology.Document;

public class ClientGui extends JFrame {
	private JTextField txtRemotePath;
	JFileChooser fileChooser;
	GuiAgent guiAgent;
	private JTextField txtLocalPath;
	private JTextField txtOwner;

	public ClientGui(GuiAgent guiAgent) {
		VersionManager versionManager = new VersionManager();
		this.guiAgent = guiAgent;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 615, 354);

		JButton btnShow = new JButton("G\u00F6r\u00FCnt\u00FCle");
		btnShow.setBounds(30, 210, 79, 23);
		btnShow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String absolutePath = txtLocalPath.getText();
				String localFilePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) + 1);
				String fileName = absolutePath.substring(absolutePath.lastIndexOf(File.separator) + 1,
						absolutePath.length());
				guiAgent.showDocument(fileName, txtRemotePath.getText(), localFilePath);
			}
		});
		getContentPane().setLayout(null);
		getContentPane().add(btnShow);

		JButton btnAdd = new JButton("Ekle");
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileChooser == null) {
					fileChooser = new JFileChooser();
				}
				int returnVal = fileChooser.showOpenDialog(ClientGui.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();

					guiAgent.addDocument(file.getName(), txtRemotePath.getText(),
							file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator)),
							"");
				} else {

				}
			}
		});
		btnAdd.setBounds(119, 210, 51, 23);
		getContentPane().add(btnAdd);

		JButton btnUpdate = new JButton("G\u00FCncelle");
		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (fileChooser == null) {
					fileChooser = new JFileChooser();
				}
				int returnVal = fileChooser.showOpenDialog(ClientGui.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					// dosya adı, hangi path'e yazılacak ve lokalde nerede
					Document document;
					try {
						String absolutePath = file.getAbsolutePath();
						String localFilePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) + 1);
						document = versionManager.getVersionline(localFilePath, file.getName());
						if (document != null) {
							guiAgent.updateDocument(document.getDocumentname(), document.getRemotedocumentpath(),
									localFilePath, document.getLocalversion(), txtOwner.getText());
						} else {
							showMessage("Lokal versiyonu olmayan bir dosya seçtiniz.");
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {

				}
			}
		});
		btnUpdate.setBounds(180, 210, 73, 23);
		getContentPane().add(btnUpdate);

		JButton btnDelete = new JButton("Sil");
		btnDelete.setBounds(263, 210, 43, 23);
		getContentPane().add(btnDelete);

		JSeparator separator = new JSeparator();
		separator.setBounds(350, 15, 0, 2);
		getContentPane().add(separator);

		txtRemotePath = new JTextField();
		txtRemotePath.setBounds(119, 106, 187, 20);
		getContentPane().add(txtRemotePath);
		txtRemotePath.setColumns(10);

		JLabel lblNewLabel = new JLabel("Remote Dosya Dizini");
		lblNewLabel.setBounds(10, 109, 99, 14);
		getContentPane().add(lblNewLabel);

		JLabel lblLokalDosyaDizini = new JLabel("Lokal Dosya Dizini");
		lblLokalDosyaDizini.setBounds(10, 63, 99, 14);
		getContentPane().add(lblLokalDosyaDizini);

		txtLocalPath = new JTextField();
		txtLocalPath.setBounds(119, 60, 187, 20);
		getContentPane().add(txtLocalPath);
		txtLocalPath.setColumns(10);

		JButton btnSetOwner = new JButton("Değiştirme Yetkisi Al");
		btnSetOwner.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				System.out.println("baken");
				if (fileChooser == null) {
					fileChooser = new JFileChooser();
				}
				int returnVal = fileChooser.showOpenDialog(ClientGui.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();

					Document document;
					try {
						String absolutePath = file.getAbsolutePath();
						String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) + 1);
						document = versionManager.getVersionline(filePath, file.getName());
						if (document != null) {
							guiAgent.setDocumentOwner(file.getName(), document.getRemotedocumentpath(),
									txtOwner.getText());

						} else {
							showMessage("Lokal versiyonu olmayan bir dosya seçtiniz.");
						}
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} else {
				}

			}
		});
		btnSetOwner.setBounds(30, 176, 151, 23);

		getContentPane().add(btnSetOwner);

		JButton btnReleaseOwner = new JButton("Değiştirme Yetkisini Bırak");
		btnReleaseOwner.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileChooser == null) {
					fileChooser = new JFileChooser();
				}
				int returnVal = fileChooser.showOpenDialog(ClientGui.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();

					Document document;
					try {
						String absolutePath = file.getAbsolutePath();
						String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) + 1);
						document = versionManager.getVersionline(filePath, file.getName());
						if (document != null) {
							guiAgent.releaseDocumentOwner(file.getName(), document.getRemotedocumentpath(),
									txtOwner.getText());

						} else {
							showMessage("Lokal versiyonu olmayan bir dosya seçtiniz.");
						}
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} else {
				}
			}
		});
		btnReleaseOwner.setBounds(213, 176, 167, 23);
		getContentPane().add(btnReleaseOwner);

		JLabel lblNewLabel_1 = new JLabel("Döküman Sahipliği");
		lblNewLabel_1.setBounds(10, 151, 99, 14);
		getContentPane().add(lblNewLabel_1);

		txtOwner = new JTextField();
		txtOwner.setBounds(119, 145, 187, 20);
		getContentPane().add(txtOwner);
		txtOwner.setColumns(10);
	}

	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int) screenSize.getWidth() / 2;
		int centerY = (int) screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setSize(400, 300);
		super.setVisible(true);
	}

	public void showMessage(String message) {
		JOptionPane.showMessageDialog(new Frame(), message);

	}
}
