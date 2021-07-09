
  ###  TrustLessFileServer(p2P)
  
   - clone the project
   - mvn exec:java -Dexec.args="/path/to/a/file"
   
  ## Run the test
    
   - mvn clean verify
   
  ###  Assumptions
  
   - The filler hash value of zero 
   - Currently used  Arrays.fill(zeroByte, (byte) 0);
   - Other options may be "0".getBytes()/BigInteger.valueOf(0).bytesArray()
   
  ### Test case
  
   - test_getBinaryContentAndItsProofPieces_AndAbleToRecreate_Tree() method is the way file is verified on the client side
  
    
    
  
