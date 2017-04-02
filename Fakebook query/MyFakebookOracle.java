package project2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyFakebookOracle extends FakebookOracle {

    static String prefix = "syzhao.";

    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;

    // You must refer to the following variables for the corresponding tables in your database
    String cityTableName = null;
    String userTableName = null;
    String friendsTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;
    String programTableName = null;
    String educationTableName = null;
    String eventTableName = null;
    String participantTableName = null;
    String albumTableName = null;
    String photoTableName = null;
    String coverPhotoTableName = null;
    String tagTableName = null;


    // DO NOT modify this constructor
    public MyFakebookOracle(String dataType, Connection c) {
        super();
        oracleConnection = c;
        // You will use the following tables in your Java code
        cityTableName = prefix + dataType + "_CITIES";
        userTableName = prefix + dataType + "_USERS";
        friendsTableName = prefix + dataType + "_FRIENDS";
        currentCityTableName = prefix + dataType + "_USER_CURRENT_CITY";
        hometownCityTableName = prefix + dataType + "_USER_HOMETOWN_CITY";
        programTableName = prefix + dataType + "_PROGRAMS";
        educationTableName = prefix + dataType + "_EDUCATION";
        eventTableName = prefix + dataType + "_USER_EVENTS";
        albumTableName = prefix + dataType + "_ALBUMS";
        photoTableName = prefix + dataType + "_PHOTOS";
        tagTableName = prefix + dataType + "_TAGS";
    }


    @Override
    // ***** Query 0 *****
    // This query is given to your for free;
    // You can use it as an example to help you write your own code
    //
    public void findMonthOfBirthInfo() {

        // Scrollable result set allows us to read forward (using next())
        // and also backward.
        // This is needed here to support the user of isFirst() and isLast() methods,
        // but in many cases you will not need it.
        // To create a "normal" (unscrollable) statement, you would simply call
        // Statement stmt = oracleConnection.createStatement();
        //
        try (Statement stmt =
                     oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                             ResultSet.CONCUR_READ_ONLY)) {

            // For each month, find the number of users born that month
            // Sort them in descending order of count
            ResultSet rst = stmt.executeQuery("select count(*), month_of_birth from " +
                    userTableName +
                    " where month_of_birth is not null group by month_of_birth order by 1 desc");

            this.monthOfMostUsers = 0;
            this.monthOfLeastUsers = 0;
            this.totalUsersWithMonthOfBirth = 0;

            // Get the month with most users, and the month with least users.
            // (Notice that this only considers months for which the number of users is > 0)
            // Also, count how many total users have listed month of birth (i.e., month_of_birth not null)
            //
            while (rst.next()) {
                int count = rst.getInt(1);
                int month = rst.getInt(2);
                if (rst.isFirst())
                    this.monthOfMostUsers = month;
                if (rst.isLast())
                    this.monthOfLeastUsers = month;
                this.totalUsersWithMonthOfBirth += count;
            }

            // Get the names of users born in the "most" month
            rst = stmt.executeQuery("select user_id, first_name, last_name from " +
                    userTableName + " where month_of_birth=" + this.monthOfMostUsers);
            while (rst.next()) {
                Long uid = rst.getLong(1);
                String firstName = rst.getString(2);
                String lastName = rst.getString(3);
                this.usersInMonthOfMost.add(new UserInfo(uid, firstName, lastName));
            }

            // Get the names of users born in the "least" month
            rst = stmt.executeQuery("select first_name, last_name, user_id from " +
                    userTableName + " where month_of_birth=" + this.monthOfLeastUsers);
            while (rst.next()) {
                String firstName = rst.getString(1);
                String lastName = rst.getString(2);
                Long uid = rst.getLong(3);
                this.usersInMonthOfLeast.add(new UserInfo(uid, firstName, lastName));
            }

            // Close statement and result set
            rst.close();
            stmt.close();
        } catch (SQLException err) {
            System.err.println(err.getMessage());
        }
    }

    @Override
    // ***** Query 1 *****
    // Find information about users' names:
    // (1) The longest first name (if there is a tie, include all in result)
    // (2) The shortest first name (if there is a tie, include all in result)
    // (3) The most common first name, and the number of times it appears (if there
    //      is a tie, include all in result)
    //
    public void findNameInfo() { // Query1
        // Find the following information from your database and store the information as shown
        //this.longestFirstNames.add("JohnJacobJingleheimerSchmidt");
        //this.shortestFirstNames.add("Al");
        //this.shortestFirstNames.add("Jo");
        //this.shortestFirstNames.add("Bo");
        //this.mostCommonFirstNames.add("John");
        //this.mostCommonFirstNames.add("Jane");
        //this.mostCommonFirstNamesCount = 10;
        
        int longestFirstNameLength = 0;
        int shortestFirstNameLength = 0;
        //this.longestFirstNameLength = 0;
        //this.shortestFirstNameLength = 0;

        try (Statement stmt =
            oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) 
        {
            ResultSet rst = stmt.executeQuery(" SELECT DISTINCT first_name FROM " + userTableName + " ORDER BY LENGTH(first_name) DESC, first_name ");
            while (rst.next()) 
            {
                String firstName = rst.getString(1);
                
                if (rst.isFirst())
                {
                    longestFirstNameLength = firstName.length();
                    this.longestFirstNames.add(firstName);
                }               
                else if (firstName.length() == longestFirstNameLength)
                {
                    this.longestFirstNames.add(firstName);
                }
            }

            rst = stmt.executeQuery(" SELECT DISTINCT first_name FROM " + userTableName + " ORDER BY LENGTH(first_name) ");
            while (rst.next()) 
            {
                String firstName = rst.getString(1);
                if (rst.isFirst())
                {
                    shortestFirstNameLength = firstName.length();
                    this.shortestFirstNames.add(firstName);
                }               
                else if (firstName.length() == shortestFirstNameLength)
                {
                    this.shortestFirstNames.add(firstName);
                }
            }

            rst = stmt.executeQuery(" SELECT Count(*) , first_name FROM " + userTableName + " GROUP BY first_name ORDER BY Count(*) DESC ");
            while (rst.next()) 
            {
                int count = rst.getInt(1);
                String firstName = rst.getString(2);
                if (rst.isFirst())
                {
                    this.mostCommonFirstNamesCount = count;
                    this.mostCommonFirstNames.add(firstName);
                
                } 
                else if (count == this.mostCommonFirstNamesCount) 
                {
                    this.mostCommonFirstNames.add(firstName);
                }             

            }
            rst.close();
            stmt.close();            
        }

        catch (SQLException err) 
        {
            System.err.println(err.getMessage());
        }

    }

    @Override
    // ***** Query 2 *****
    // Find the user(s) who have no friends in the network
    //
    // Be careful on this query!
    // Remember that if two users are friends, the friends table
    // only contains the pair of user ids once, subject to
    // the constraint that user1_id < user2_id
    //
    public void lonelyUsers() {
        // Find the following information from your database and store the information as shown
        
        //this.lonelyUsers.add(new UserInfo(10L, "Billy", "SmellsFunny"));
        //this.lonelyUsers.add(new UserInfo(11L, "Jenny", "BadBreath"));

        try (Statement stmt =
            oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) 
        {
            ResultSet rst = stmt.executeQuery("SELECT user_id, first_name, last_name FROM " 
            + userTableName + " MINUS ( SELECT U.user_id, U.first_name, U.last_name FROM "
            + friendsTableName + " F, " 
            + userTableName + " U WHERE F.user1_id = U.user_id UNION SELECT U.user_id, U.first_name, U.last_name FROM "
            + friendsTableName + " F, " 
            + userTableName + " U WHERE F.user2_id = U.user_id) ORDER BY user_id ");
            while (rst.next()) 
            {
                Long uid = rst.getLong(1);
                String firstName = rst.getString(2);
                String lastName = rst.getString(3);
                this.usersInMonthOfMost.add(new UserInfo(uid, firstName, lastName));

            }

            rst.close();
            stmt.close();
        } 

        catch (SQLException err) 
        {
            System.err.println(err.getMessage());
        }
    }

    @Override
    // ***** Query 3 *****
    // Find the users who do not live in their hometowns
    // (I.e., current_city != hometown_city)
    //
    public void liveAwayFromHome() throws SQLException {
            try (Statement stmt = 
                oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)) 
            {
            ResultSet rst = stmt.executeQuery("select U.user_id, U.first_name, U.last_name from " +
                    userTableName +" U, " + hometownCityTableName + " H, " + currentCityTableName + " C " +
                    "where U.user_id = C.user_id and H.user_id = U.user_id and H.hometown_city_id <> C.current_city_id and (H.hometown_city_id is not null) order by user_id"); 


            //this.liveAwayFromHome.add(new UserInfo(10L, "bob", "Mvalot"));

            while(rst.next())
            {
                Long uid = rst.getLong(1);
                String firstname = rst.getString(2);
                String lastname = rst.getString(3);
                
                this.liveAwayFromHome.add(new UserInfo(uid, firstname, lastname));
            }

        rst.close();
        stmt.close();
        }catch(SQLException err){
            System.err.println(err.getMessage());
        }
    }

    @Override
    // **** Query 4 ****
    // Find the top-n photos based on the number of tagged users
    // If there are ties, choose the photo with the smaller numeric PhotoID first
    //
    public void findPhotosWithMostTags(int n) {
        // String photoId = "1234567";
        // String albumId = "123456789";
        // String albumName = "album1";
        // String photoCaption = "caption1";
        // String photoLink = "http://google.com";
        // PhotoInfo p = new PhotoInfo(photoId, albumId, albumName, photoCaption, photoLink);
        // TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
        // tp.addTaggedUser(new UserInfo(12345L, "taggedUserFirstName1", "taggedUserLastName1"));
        // tp.addTaggedUser(new UserInfo(12345L, "taggedUserFirstName2", "taggedUserLastName2"));
        // this.photosWithMostTags.add(tp);

        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) 
        {
            ResultSet rst = stmt.executeQuery(" SELECT tag_photo_id, count(*) FROM " 
            + tagTableName + " GROUP BY tag_photo_id ORDER BY count(*) DESC, tag_photo_id ");

            int count = 1;
         

            while( rst.next() && count <= n )
                {
                   String photoId = rst.getString(1);
                    TaggedPhotoInfo tp = null;
                    PhotoInfo p = null;

                    try( Statement stmt1 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY))
                    {
                        ResultSet rst1 = stmt1.executeQuery(" SELECT A.album_id, A.album_name, P.photo_caption, P.photo_link FROM " 
                            + photoTableName + " P , " 
                            + albumTableName + " A WHERE P.photo_id = " 
                            + photoId + " AND A.album_id = P.album_id ");

                        while (rst1.next())
                        {
                            String albumId = rst1.getString(1);
                            String albumName = rst1.getString(2);
                            String photoCaption = rst1.getString(3);
                            String photoLink = rst1.getString(4);
                            p = new PhotoInfo(photoId, albumId, albumName, photoCaption, photoLink);
                            tp = new TaggedPhotoInfo(p);
                        }

                    
                   
                        rst1 = stmt1.executeQuery(" SELECT U.user_id, U.first_name, U.last_name FROM " 
                            + userTableName + " U , " 
                            + tagTableName + " T WHERE T.tag_photo_id = "
                            + photoId + " AND U.user_id = T.tag_subject_id ");

                        while(rst1.next())
                        {
                            Long uid = rst1.getLong(1);
                            String firstName = rst1.getString(2);
                            String lastName = rst1.getString(3);
                            tp.addTaggedUser(new UserInfo(uid, firstName, lastName));
                            this.photosWithMostTags.add(tp);
                        }

                        rst1.close();
                        stmt1.close();
                    }
                    
                    count++;
                }
            
            rst.close();
            stmt.close();

        } 
        catch (SQLException err) 
        {
            System.err.println(err.getMessage());
        }
    }

    @Override
    // **** Query 5 ****
    // Find suggested "match pairs" of users, using the following criteria:
    // (1) Both users should be of the same gender
    // (2) They should be tagged together in at least one photo (They do not have to be friends of the same person)
    // (3) Their age difference is <= yearDiff (just compare the years of birth for this)
    // (4) They are not friends with one another
    //
    // You should return up to n "match pairs"
    // If there are more than n match pairs, you should break ties as follows:
    // (i) First choose the pairs with the largest number of shared photos
    // (ii) If there are still ties, choose the pair with the smaller user1_id
    // (iii) If there are still ties, choose the pair with the smaller user2_id
    //
    public void matchMaker(int n, int yearDiff) {
            try (Statement stmt = oracleConnection.createStatement()) {
                stmt.executeUpdate("create view PhotosofFriends as select U1.user_id as u1id, U2.user_id as u2id ,count(*) as cont"
                    + " from " + userTableName + " U1, " + userTableName + " U2, " + tagTableName + " T1, " + tagTableName + " T2 "
                    + " where U1.user_id = T1.tag_subject_id and U2.user_id = T2.tag_subject_id and T1.tag_photo_id = T2.tag_photo_id "
                    + " and U1.user_id < U2.user_id and U1.gender = U2.gender "
                    + " and U1.year_of_birth is not null and U2.year_of_birth is not null and abs(U1.year_of_birth - U2.year_of_birth) < = " + yearDiff
                    + " group by U1.user_id,U2.user_id order by cont DESC, u1id ASC, u2id ASC");

                ResultSet rst = stmt.executeQuery("select U1.user_id, U1.first_name, U1.last_name, U1.year_of_birth, U2.user_id, U2.first_name, U2.last_name, U2.year_of_birth, P.photo_id, P.album_id, A.album_name, P.photo_caption, P.photo_link"
                    + " from " + userTableName + " U1, " + userTableName + " U2, " + " PhotosofFriends s, " + photoTableName + " P, " + albumTableName + " A, " + tagTableName + " T1, " + tagTableName + " T2 " 
                    + " where (U1.user_id, U2.user_id )in (select s.u1id, s.u2id from PhotosofFriends s) "
                    + " and s.u1id = U1.user_id and s.u2id = U2.user_id "
                    + " and U1.user_id = T1.tag_subject_id and U2.user_id = T2.tag_subject_id and T1.tag_photo_id = T2.tag_photo_id "
                    + " and not exists( select F.user1_id ,F.user2_id from " + friendsTableName + " F where U1.user_id = F.user1_id and U2.user_id = F.user2_id) "
                    + " and T1.tag_photo_id = P.photo_id and P.album_id = A.album_id and rownum <= " + n 
                    + " order by s.cont DESC ,U1.user_id ASC,  U2.user_id ASC");
                //initial
                Long temp1 = 123L;
                Long temp2 = 123L;
                MatchPair mp = new MatchPair(temp1, "Firstname", "Lastname", 0, temp2, "Firstname", "Lastname",0);  

                while(rst.next()){
                    long u1UserId = rst.getLong(1);
                    long u2UserId= rst.getLong(5);
                        if ( u1UserId!=temp1 || u2UserId!= temp2){
                            String u1FirstName = rst.getString(2);
                            String u1LastName = rst.getString(3);
                            int u1Year = rst.getInt(4);
                            String u2FirstName = rst.getString(6);
                            String u2LastName = rst.getString(7);
                            int u2Year = rst.getInt(8);
                            mp = new MatchPair(u1UserId, u1FirstName, u1LastName, u1Year, u2UserId, u2FirstName, u2LastName,u2Year);
                            temp1 = u1UserId; temp2= u2UserId;
                        }
                    String sharedPhotoId = rst.getString(9);
                    String sharedPhotoAlbumId = rst.getString(10);
                    String sharedPhotoAlbumName = rst.getString(11);
                    String sharedPhotoCaption =rst.getString(12);
                    String sharedPhotoLink = rst.getString(13);
                    mp.addSharedPhoto(new PhotoInfo(sharedPhotoId, sharedPhotoAlbumId, sharedPhotoAlbumName, sharedPhotoCaption,sharedPhotoLink));          
                  }
                this.bestMatches.add(mp);
                stmt.executeUpdate("drop view PhotosofFriends");       
                rst.close();
                stmt.close();
            } 
            catch (SQLException err) 
            {
                System.err.println(err.getMessage());
            }
    }

    // **** Query 6 ****
    // Suggest users based on mutual friends
    //
    // Find the top n pairs of users in the database who have the most
    // common friends, but are not friends themselves.
    //
    // Your output will consist of a set of pairs (user1_id, user2_id)
    // No pair should appear in the result twice; you should always order the pairs so that
    // user1_id < user2_id
    //
    // If there are ties, you should give priority to the pair with the smaller user1_id.
    // If there are still ties, give priority to the pair with the smaller user2_id.
    //
    @Override
    public void suggestFriendsByMutualFriends(int n) {
        // Long user1_id = null;
        // String user1FirstName = null;
        // String user1LastName = null;
        // Long user2_id = null;
        // String user2FirstName = null;
        // String user2LastName = null;
        // UsersPair p = null;

        // p.addSharedFriend(567L, "sharedFriend1FirstName", "sharedFriend1LastName");
        // p.addSharedFriend(678L, "sharedFriend2FirstName", "sharedFriend2LastName");
        // p.addSharedFriend(789L, "sharedFriend3FirstName", "sharedFriend3LastName");
        // this.suggestedUsersPairs.add(p);
        
        // ArrayList<Long> u1_id = new ArrayList<Long>();
        // ArrayList<Long> u2_id = new ArrayList<Long>();

        // long[] u1_id = new long[n];
        // long[] u2_id = new long[n];

        try (Statement stmt =
            oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) 
        {

            long[] u1_id = new long[n];
            long[] u2_id = new long[n];

            Long user1_id = null;
            String user1FirstName = null;
            String user1LastName = null;
            Long user2_id = null;
            String user2FirstName = null;
            String user2LastName = null;
            UsersPair p = null;

            String fastStatement =  " SELECT u1, u2, count(*) FROM (SELECT F1.user1_id AS u1 , F2.user1_id AS u2 FROM " 
            + friendsTableName + " F1, " 
            + friendsTableName + " F2 WHERE F1.user2_id = F2.user2_id AND F1.user1_id < F2.user1_id UNION ALL SELECT F1.user2_id AS u1 , F2.user2_id AS u2 FROM "
            + friendsTableName + " F1, "
            + friendsTableName + " F2 WHERE F1.user1_id = F2.user1_id AND F1.user2_id < F2.user2_id UNION ALL SELECT F1.user1_id AS u1 , F2.user2_id AS u2 FROM "
            + friendsTableName + " F1, " 
            + friendsTableName + " F2 WHERE F1.user2_id = F2.user1_id AND F1.user1_id < F2.user2_id ) WHERE NOT EXISTS (SELECT * FROM "
            + friendsTableName + " F WHERE (F.user1_id = u1 AND F.user2_id = u2 )) GROUP BY u1, u2 ORDER BY count(*) DESC, u1, u2 " ;

            String slowStatement = " SELECT U1.user_id, U2.user_id, count(*) FROM " 
            + userTableName + " U1, " 
            + userTableName + " U2, "
            + friendsTableName + " F1, " 
            + friendsTableName + " F2 WHERE U1.user_id < U2.user_id "
            + " AND ((U1.user_id = F1.user1_id AND U2.user_id = F2.user1_id AND F1.user2_id = F2.user2_id) "
            + " OR (U1.user_id = F1.user2_id AND U2.user_id = F2.user2_id AND F1.user1_id = F2.user1_id) " 
            + " OR (U1.user_id = F1.user1_id AND U2.user_id = F2.user2_id AND F1.user2_id = F2.user1_id)) AND NOT EXISTS "
            + " ( SELECT F3.user1_id, F3.user2_id FROM syzhao.PUBLIC_FRIENDS F3 WHERE (U1.user_id = F3.user1_id AND U2.user_id = F3.user2_id)) "
            + " GROUP BY U1.user_id, U2.user_id "
            + " ORDER BY count(*) DESC, U1.user_id, U2.user_id " ;

            ResultSet rst = stmt.executeQuery(slowStatement);

            // ResultSet rst = stmt.executeQuery(" SELECT U1.user_id, U2.user_id, count(*) FROM " 
            //     + userTableName + " U1, " 
            //     + userTableName + " U2, "
            //     + friendsTableName + " F1, " 
            //     + friendsTableName + " F2 WHERE U1.user_id < U2.user_id "
            //     + " AND ((U1.user_id = F1.user1_id AND U2.user_id = F2.user1_id AND F1.user2_id = F2.user2_id) "
            //     + " OR (U1.user_id = F1.user2_id AND U2.user_id = F2.user2_id AND F1.user1_id = F2.user1_id) " 
            //     + " OR (U1.user_id = F1.user1_id AND U2.user_id = F2.user2_id AND F1.user2_id = F2.user1_id)) AND NOT EXISTS "
            //     + " ( SELECT F3.user1_id, F3.user2_id FROM syzhao.PUBLIC_FRIENDS F3 WHERE (U1.user_id = F3.user1_id AND U2.user_id = F3.user2_id)) "
            //     + " GROUP BY U1.user_id, U2.user_id "
            //     + " ORDER BY count(*) DESC, U1.user_id, U2.user_id " );

            int count = 0;

            while ( rst.next() && count < n)
            {
                // u1_id.add( (rst.getLong(1)) );
                // u2_id.add( (rst.getLong(2)) );

                u1_id[count] = rst.getLong(1);
                u2_id[count] = rst.getLong(2);
                 // userspairs[count][0] = rst.getLong(1);
                 // userspairs[count][1] = rst.getLong(2);
                 count++;
            }

            // for (int i = 0; i < n; i++)
            // {
            //     System.out.println(u1_id[i]);
            //     System.out.println(u2_id[i]);
            // }


            for ( int x = 0; x < n; x++)
            {

                Long sharedFriendId = null;
                String sharedFriendFirstName = null;
                String sharedFriendLastName = null;

                rst = stmt.executeQuery(" SELECT user_id , first_name, last_name FROM "
                    + userTableName + " WHERE user_id = " + u1_id[x]);

                while(rst.next())
                {
                    user1_id = rst.getLong(1);
                    user1FirstName = rst.getString(2);
                    user1LastName = rst.getString(3);
                }

                rst = stmt.executeQuery(" SELECT user_id , first_name, last_name FROM "
                    + userTableName + " WHERE user_id = " + u2_id[x]);

                while(rst.next())
                {
                    user2_id = rst.getLong(1);
                    user2FirstName = rst.getString(2);
                    user2LastName = rst.getString(3);
                }

                // System.out.println(user1_id);
                // System.out.println(user2_id);

                p = new UsersPair(user1_id, user1FirstName, user1LastName, user2_id, user2FirstName, user2LastName);

                rst = stmt.executeQuery(" SELECT U.user_id , U.first_name, U.last_name FROM "
                    + userTableName + " U, " 
                    + friendsTableName + " F1, " 
                    + friendsTableName + " F2 WHERE ( F1.user1_id = " + user1_id + " AND F1.user2_id = U.user_id AND F2.user1_id = " + user2_id + " AND F2.user2_id = U.user_id) " 
                    + " OR ( F1.user1_id = " + user1_id + " AND F1.user2_id = U.user_id AND F2.user2_id = " + user2_id + " AND F2.user1_id = U.user_id) "  
                    + " OR ( F1.user2_id = " + user1_id + " AND F1.user1_id = U.user_id AND F2.user2_id = " + user2_id + " AND F2.user1_id = U.user_id) ORDER BY U.user_id ");

                while(rst.next())
                {
                    sharedFriendId = rst.getLong(1);
                    sharedFriendFirstName = rst.getString(2);
                    sharedFriendLastName = rst.getString(3);
                    // System.out.println(sharedFriendId);
                    p.addSharedFriend( sharedFriendId, sharedFriendFirstName, sharedFriendLastName );
                }

                this.suggestedUsersPairs.add(p);
            }

            //System.out.println(Arrays.toString(userspairs));
            //System.out.println(Arrays.toString(friendcounts));
            rst.close();
            stmt.close();            
        }

        catch (SQLException err) 
        {
            System.err.println(err.getMessage());
        }
        
    }

    @Override
    // ***** Query 7 *****
    //
    // Find the name of the state with the most events, as well as the number of
    // events in that state.  If there is a tie, return the names of all of the (tied) states.
    //
    public void findEventStates() {
        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
             ResultSet.CONCUR_READ_ONLY)) 
        {

            ResultSet rst = stmt.executeQuery("select count (*), C.state_name from " + cityTableName +" C, " + eventTableName + " E " + 
                "where (C.city_id = E.event_city_id) group by C.state_name order by 1 desc");
            
            while(rst.next())
            {
                if(rst.isFirst())
                {
                    this.eventCount = rst.getInt(1);
                    this.popularStateNames.add(rst.getString(2));
                }
                else if(rst.getInt(1) ==  this.eventCount)
                {
                    this.popularStateNames.add(rst.getString(2));
                }
            }            
            //this.eventCount = 12;
            //this.popularStateNames.add("Michigan");
            //this.popularStateNames.add("California");

        rst.close();
        stmt.close();
        }
        catch(SQLException err)
        {
            System.err.println(err.getMessage());
        }
        
    }

    //@Override
    // ***** Query 8 *****
    // Given the ID of a user, find information about that
    // user's oldest friend and youngest friend
    //
    // If two users have exactly the same age, meaning that they were born
    // on the same day, then assume that the one with the larger user_id is older
    //
    public void findAgeInfo(Long user_id) {
        //this.oldestFriend = new UserInfo(1L, "Oliver", "Oldham");
        //this.youngestFriend = new UserInfo(25L, "Yolanda", "Young");

        try (Statement stmt =
            oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY)) 
        {
            ResultSet rst = stmt.executeQuery(" SELECT U.user_id, U.first_name, U.last_name, U.year_of_birth, U.month_of_birth, U.day_of_birth FROM " 
               + userTableName + " U, " + friendsTableName + " F WHERE (( F.user2_id = " 
               + user_id + " AND F.user1_id = U.user_id ) OR ( F.user1_id = " 
               + user_id + " AND F.user2_id = U.user_id )) ORDER BY U.year_of_birth, U.month_of_birth, U.day_of_birth, U.user_id DESC ");

            while (rst.next())
            {
                if (rst.first())
                {
                    Long uid = rst.getLong(1);
                    String firstName = rst.getString(2);
                    String lastName = rst.getString(3);
                    this.oldestFriend = new UserInfo(uid, firstName, lastName);
                }

                if (rst.last())
                {
                    Long uid = rst.getLong(1);
                    String firstName = rst.getString(2);
                    String lastName = rst.getString(3);
                    this.youngestFriend = new UserInfo(uid, firstName, lastName);
                }

            }


            rst.close();
            stmt.close();            
        }

        catch (SQLException err) 
        {
            System.err.println(err.getMessage());
        }

    }

    @Override
    //	 ***** Query 9 *****
    //
    // Find pairs of potential siblings.
    //
    // A pair of users are potential siblings if they have the same last name and hometown, if they are friends, and
    // if they are less than 10 years apart in age.  Pairs of siblings are returned with the lower user_id user first
    // on the line.  They are ordered based on the first user_id and in the event of a tie, the second user_id.
    //
    //
    public void findPotentialSiblings() {
        // Long user1_id = 123L;
        // String user1FirstName = "User1FirstName";
        // String user1LastName = "User1LastName";
        // Long user2_id = 456L;
        // String user2FirstName = "User2FirstName";
        // String user2LastName = "User2LastName";
        // SiblingInfo s = new SiblingInfo(user1_id, user1FirstName, user1LastName, user2_id, user2FirstName, user2LastName);
        // this.siblings.add(s);
        try(Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)) 
        {
            ResultSet rst = stmt.executeQuery("SELECT U1.USER_ID, U2.USER_ID, U1.FIRST_NAME, U2.FIRST_NAME, U1.LAST_NAME, U2.LAST_NAME FROM " 
                + userTableName + " U1, " + userTableName + " U2, " + hometownCityTableName + " H1, " + hometownCityTableName + " H2, " + friendsTableName + " f "
                +"WHERE (U1.LAST_NAME = U2.LAST_NAME) "
                +"AND (U1.USER_ID = H1.USER_ID) " + "AND (U2.USER_ID = H2.USER_ID) "
                +"AND (H1.HOMETOWN_CITY_ID = H2.HOMETOWN_CITY_ID) "
                +"AND (((U1.USER_ID = f.USER1_ID) AND (U2.USER_ID = f.USER2_ID)) OR ((U1.USER_ID = f.USER2_ID) AND (U2.USER_ID = f.USER1_ID))) "
                +"AND (ABS(U2.YEAR_OF_BIRTH - U1.YEAR_OF_BIRTH) < 10) "
                +"AND (U1.USER_ID < U2.USER_ID) " + "ORDER BY U1.USER_ID, U2.USER_ID ASC");

            while(rst.next())
            {
                Long user1_id = rst.getLong(1);
                Long user2_id = rst.getLong(2);
                String user1FirstName = rst.getString(3);
                String user2FirstName = rst.getString(4);
                String user1LastName = rst.getString(5);
                String user2LastName = rst.getString(6);
                SiblingInfo s = new SiblingInfo(user1_id, user1FirstName, user1LastName, user2_id, user2FirstName, user2LastName);
                this.siblings.add(s);
            }

            rst.close();
            stmt.close();
        }
        catch(SQLException err)
        {
            System.err.println(err.getMessage());
        
        }
    }

}
