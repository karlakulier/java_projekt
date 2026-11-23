package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jms;

import edu.unizg.foi.nwtis.Korisnik;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;

@MessageDriven(mappedName = "jms/NWTiSTopic",
    activationConfig = {
        @ActivationConfigProperty(propertyName = "acknowledgeMode",
            propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "jakarta.jms.Topic")})
public class PrimateljJmsTema implements MessageListener {

  @Inject
  SpremnikJmsPoruka spremnikJmsPoruka;

  public PrimateljJmsTema() {}

  @Override
  public void onMessage(Message message) {
    try {
      if (message instanceof ObjectMessage objectMessage) {
        Object objekt = objectMessage.getObject();

        if (objekt instanceof Korisnik korisnik) {
          System.out.println("Stigla ObjectMessage poruka u temu: "
              + message.getJMSMessageID() + " korisnik: " + korisnik.korisnik());

          this.spremnikJmsPoruka.dodajPorukuTeme(korisnik);
        }

        return;
      }

      if (message instanceof TextMessage textMessage) {
        String tekst = textMessage.getText();

        System.out.println("Stigla TextMessage poruka u temu: "
            + message.getJMSMessageID() + " " + tekst);

        /*
         * Za nadzornu ploču korisnika trebamo zapis Korisnik.
         * Zato tekstualne poruke samo ispisujemo, ali ih ne dodajemo u listu korisnika.
         */
      }

    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
}