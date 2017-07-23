package guiGecoSi;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;

import gnu.io.CommPortIdentifier;
import net.gecosi.CommStatus;
import net.gecosi.SiHandler;
import net.gecosi.SiListener;
import net.gecosi.dataframe.SiDataFrame;

import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JButton;

public class Fenetre {

	private JFrame frame;
	
	private JLabel lblPort;
	private JLabel lblPuce;
	
	private JButton btnArretLecture;
	private JButton btnConnexion;
	private JButton btnDemarrerLecture;
	
	private SiHandler handler;
	
	private String portActif;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Fenetre window = new Fenetre();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Fenetre() {
		initialize();
		this.frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("Fichier");
		menuBar.add(mnNewMenu);
		
		JMenuItem mnQuitter = new JMenuItem("Quitter");
		mnQuitter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		mnNewMenu.add(mnQuitter);
		
		lblPort = new JLabel("Port : ");
		lblPort.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(lblPort, BorderLayout.NORTH);
		
		lblPuce = new JLabel("puce ->");
		frame.getContentPane().add(lblPuce, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnDemarrerLecture = new JButton("Démarrer la lecture");
		btnDemarrerLecture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lanceLecture();
			}
		});
		panel.add(btnDemarrerLecture);
		
		btnConnexion = new JButton("Connexion en cours");
		btnConnexion.setEnabled(false);
		btnConnexion.setVisible(false);
		panel.add(btnConnexion);
		
		btnArretLecture = new JButton("Arrêter la lecture");
		btnArretLecture.setVisible(false);
		btnArretLecture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				arreteLecture();
			}
		});
		
		panel.add(btnArretLecture);
	}
	

	
	/**
	 * Démarre la lecture sur le port COM3
	 */
	private void lanceLecture() {
		//lanceLecture("COM3"); // windows
		//lanceLecture("/dev/ttyUSB0"); //Linux
		java.util.Enumeration<CommPortIdentifier> portEnum = listPorts();
		CommPortIdentifier portIdentifier = portEnum.nextElement();
		String port = portIdentifier.getName();
		if (portEnum.hasMoreElements()){
			lblPuce.setText("Vous devez choisir un port");
		} else {
			portActif = port;
			lanceLecture(port);
		}
	}
	
	/**
	 * Démarre la lecture sur le port COM3
	 */
	/**
	 * Démarre la lecture sur le port pPort puis écoute le port
	 * 
	 * @param pPort port à utiliser
	 */
	private void lanceLecture(String pPort) {
		System.out.println(pPort);
		
		lecteurConnexion();

		handler = new SiHandler(new SiListener() {
			public void handleEcard(SiDataFrame dataFrame) {
				dataFrame.printString();
				afficheDonnees(dataFrame);
			}
			public void notify(CommStatus status) {
				System.out.println("Status -> " + status);
				switch(status.toString()) {
					case "READY":
						lecteurPret(pPort);
						break;
					case "STARTING":
					case "PROCESSING":
						lecteurLecture();
						break;
					case "OFF":
						lecteurArret();
						break;
					default:
						lecteurArret();
				}
				
			}
			public void notify(CommStatus errorStatus, String errorMessage) {
				System.out.println("Error -> " + errorStatus + " - " + errorMessage);
				lblPuce.setText("Error -> " + errorStatus + " - " + errorMessage);
				lecteurArret();
			}
		});
		
		try {
			handler.connect(pPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Affiche l'état en cours de connexion
	 */
	private void lecteurConnexion() {
		btnArretLecture.setVisible(false);
		btnConnexion.setVisible(true);
		btnDemarrerLecture.setVisible(false);
	}

	/**
	 * Affiche l'état en cours de lecture de puce
	 */
	private void lecteurLecture() {
		lecteurConnexion();
	}
	
	/**
	 * Affiche l'état Lecteur prêt et le port utilisé
	 * 
	 * @param pPort port utilisé
	 */
	private void lecteurPret(String pPort) {
		lblPort.setText("Port : " + pPort);
		btnArretLecture.setVisible(true);
		btnConnexion.setVisible(false);
		btnDemarrerLecture.setVisible(false);
	}

	/**
	 * Affiche l'état lecteur arrêté
	 */
	private void lecteurArret() {
		btnArretLecture.setVisible(false);
		btnConnexion.setVisible(false);
		btnDemarrerLecture.setVisible(true);
	}
	
	/**
	 * Affiche les informations lues de la puce
	 * @param dataFrame les données de la puce
	 */
	private void afficheDonnees(SiDataFrame dataFrame) {
		String serie = dataFrame.getSiSeries();
		String numPuce = dataFrame.getSiNumber();
		int nbTemps = dataFrame.getNbPunches();
		String balises = " balise";
		if(nbTemps > 1) 
			balises += "s";
		balises += " ";
		lblPuce.setText("Puce -> série " + serie + " : " + numPuce + " - " + nbTemps + balises);
	}
	
	/**
	 * Arrête la lecture
	 */
	private void arreteLecture() {
		handler.stop();
	}
	
	
	
	
	/**
	 * Recherche les ports actifs
	 */
	static java.util.Enumeration<CommPortIdentifier> listPorts()
    {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()) );
        }
        portEnum = CommPortIdentifier.getPortIdentifiers();
        return portEnum;
    }
    
	/**
	 * Retourne le type de port
	 * 
	 * @param portType
	 * @return String
	 */
    static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

}
