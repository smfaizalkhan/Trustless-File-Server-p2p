
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
   - 
  ### Issue
   - coz of the filler hash assumption the last chunk pf the proof and root hash is different from the sample shared(icons_rgb_colors.png)
   - but the  test_getBinaryContentAndItsProofPieces_AndAbleToRecreate_Tree proves the integirty/validity
  
  ### EndPoints
  
   - Spark for REST EndPoints
   - Spark by default runs on port 4567
   - curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:4567/hashes
   - curl -i -H "Accept: application/json" -H "Content-Type: application/json"-X GET http://localhost:4567/piece/00491e65b5682d586fc983f9c0c8d4fc5b9f38d9cf05017eccc2b0a036935cb1/8
    
    
  
