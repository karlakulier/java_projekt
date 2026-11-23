package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jms;

import java.io.Serializable;

import edu.unizg.foi.nwtis.Korisnik;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

@Stateless
public class PosiljateljJmsTema implements Serializable {

  private static final long serialVersionUID = -5260570737291296322L;

  @Resource(mappedName = "jms/NWTiSConnectionFactory")
  private ConnectionFactory connectionFactory;

  @Resource(mappedName = "jms/NWTiSTopic")
  private Topic topic;

  public boolean novaPoruka(Korisnik korisnik) {
    boolean status = true;

    try (JMSContext context = connectionFactory.createContext()) {
      ObjectMessage message = context.createObjectMessage();
      message.setObject(korisnik);
      context.createProducer().send(topic, message);
    } catch (JMSException e) {
      status = false;
    }

    return status;
  }

  public boolean novaPoruka(String poruka) {
    boolean status = true;

    try (JMSContext context = connectionFactory.createContext()) {
      TextMessage message = context.createTextMessage();
      message.setText(poruka);
      context.createProducer().send(topic, message);
    } catch (JMSException e) {
      status = false;
    }

    return status;
  }
}