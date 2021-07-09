
  ###  TrustLessFileServer(p2P)
  
   - clone the project
   - mvn exec:java -Dexec.args="/path/to/a/file"
   
  ## Run the test
    
   - mvn clean verify
   
  ###  Assumptions
  
   - The filler hash value of zero 
   - Currently used  Arrays.fill(zeroByte, (byte) 0);
   - Other options are  "0".getBytes()/BigInteger.valueOf(0).bytesArray()
   
  ### Test case
  
   - test_getBinaryContentAndItsProofPieces_AndAbleToRecreate_Tree() method is the way peice integrity will be verified on the client side
  
  ### EndPoints
  
   - Spark for REST EndPoints
    
    
  
