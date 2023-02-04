package com.footlocer.mon.controller;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import static javax.mail.internet.MimeUtility.*;
import static org.apache.commons.io.IOUtils.copy;

public class InboxReader {

    public List<Message> ReadMailByIMAP(String username, String Password) //返回list类型的电子邮件列表。
    {
        java.util.List MessageList = new ArrayList();
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");//设置电子邮件协议

        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", username, Password);
            System.out.println(store);

            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_ONLY);
            Message messages[] = inbox.getMessages();
            for (Message message : messages) {
                MessageList.add(message);
                //System.out.println(message.getSubject());
            }
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.exit(2);
        }
        return MessageList;

    }

    public static void main(String args[]) {
        InboxReader ir = new InboxReader();
        String username ="shenfengfeng888@gmail.com";//设置用户名
        String Password = "ocngqntojzuitmbr";//设置密码
        List<Message> list= ir.ReadMailByIMAP(username, Password);
        for(int i=list.size()-1;i>0;i--){
            try {

                Message message = list.get(i);
                String date = DateUtil.format(message.getSentDate(), "yyyy/MM/dd");
                InternetAddress address[] = (InternetAddress[]) message.getFrom();

                if(address[0].getAddress().equals("nike@official.nike.com")){
                    String contentType = message.getContentType();
                    InputStream input = message.getInputStream();
                    String data = getStringByInputStream_2(input);
                    //System.out.println(data2);
                    String regex = "HBD-\\S{1,18}</p>";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(data);
                    while (matcher.find()) {
                        String group = matcher.group();
                        System.out.println(StrUtil.removeSuffix(group,"</p>") + "--------------------"+ date);
                        //System.out.println(StrUtil.removeAll(group,'r','_'); + "--------------------" + address[0].getAddress()+"------------------" + message.getSubject() + "----------------------" + date);
                    }


                }



            } catch (MessagingException ex) {
                Logger.getLogger(InboxReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String getStringByInputStream_2(InputStream inputStream){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            try {
                inputStream.close();
                bufferedReader.close();
            } catch (Exception e1) {
            }
        }
        return null;
    }



    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
            //result = parseMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());

            }
        }
        return result;
    }

}
