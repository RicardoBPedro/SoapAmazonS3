package br.com.ricardobpedro.amazons3.soap;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.Base64;
import com.amazonaws.util.IOUtils;

import javax.annotation.PostConstruct;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.WebServiceException;
import java.io.*;

/**
 * Created by ricardo on 15/01/17.
 */

@WebService
public class Soap {

    AmazonS3 s3client = null;

    @PostConstruct
    public void init() {
        s3client = new AmazonS3Client(new ProfileCredentialsProvider());
    }

    @WebMethod
    public void upload(@XmlElement(name = "BUCKET_NAME", required = true) String bucketName,
                       @XmlElement(name = "KEY_NAME", required = true) String keyName,
                       @XmlElement(name = "FILE_NAME", required = true) String uploadFileName,
                       @XmlElement(name = "CONTENT_FILE", required = true) String contentFile) {

        try {
            File file = new File(uploadFileName);
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            outputStream.write(Base64.decode(contentFile));
            outputStream.close();

            s3client.putObject(new PutObjectRequest(bucketName, keyName, file));

        } catch (AmazonServiceException ase) {
            throwAmazonServiceException(ase);

        } catch (AmazonClientException ace) {
            System.out.println("Error Message: " + ace.getMessage());

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();

        } catch (IOException ex) {
            throw new WebServiceException(ex);
        }
    }

    @WebMethod
    public String download(@XmlElement(name = "BUCKET_NAME", required = true) String bucketName,
                           @XmlElement(name = "KEY_NAME", required = true) String keyName) {
        String contentFile = null;

        try {
            S3Object s3object = s3client.getObject(new GetObjectRequest(
                    bucketName, keyName));

            contentFile = Base64.encodeAsString(IOUtils.toByteArray(s3object.getObjectContent()));

        } catch (AmazonServiceException ase) {
            throwAmazonServiceException(ase);

        } catch (AmazonClientException ace) {
            System.out.println("Error Message: " + ace.getMessage());

        } catch (IOException ex) {
            throw new WebServiceException(ex);
        }
        return contentFile;
    }

    @WebMethod
    public void delete(@XmlElement(name = "BUCKET_NAME", required = true) String bucketName,
                       @XmlElement(name = "KEY_NAME", required = true) String keyName) {

        try {
            if (s3client.doesObjectExist(bucketName, keyName)) {
                s3client.deleteObject(bucketName, keyName);
            }
        } catch (AmazonServiceException ase) {
            throwAmazonServiceException(ase);
        }
    }

    private void throwAmazonServiceException(AmazonServiceException ase) {
        System.out.println("Error Message:    " + ase.getMessage());
        System.out.println("HTTP Status Code: " + ase.getStatusCode());
        System.out.println("AWS Error Code:   " + ase.getErrorCode());
        System.out.println("Error Type:       " + ase.getErrorType());
        System.out.println("Request ID:       " + ase.getRequestId());
    }
}
