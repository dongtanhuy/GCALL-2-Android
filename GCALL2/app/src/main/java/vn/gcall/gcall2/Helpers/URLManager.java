package vn.gcall.gcall2.Helpers;

/**
 * Created by This PC on 14/06/2016.
 * Manage all API URLs
 */
public class URLManager {
    private static final String DOMAIN_NAME="https://call.gcall.vn";
    //Dev environment
//    private static final String DOMAIN_NAME="https://dev.gcall.vn";

    private static final String PREFIX_API=DOMAIN_NAME+"/api/";
    private static String SignInAPI=DOMAIN_NAME+"/api/signin";
    private static String SignUpAPI=DOMAIN_NAME+"/api/signup";
    private static String SignOutAPI=DOMAIN_NAME+"/api/signout";
    private static String InvitationAPI=DOMAIN_NAME+"/api/notifications";
    private static String ShowHotlineAPI=DOMAIN_NAME+"/api/manage";
    private static String ShowAgentHotlineAPI=DOMAIN_NAME+"/api/agent";
    private static String GetListHotlineToBuy=DOMAIN_NAME+"/api/hotline/list?country=";
    private static String BuyHotlineAPI=DOMAIN_NAME+"/api/hotline/buy";
    private static String ChangePasswordAPI=DOMAIN_NAME+"/api/password/change";
    private static String CreatCallLogAPI=DOMAIN_NAME+"/api/calllogs/create";
    private static String ShowCallogsAPI=DOMAIN_NAME+"/api/calllogs/list";
    private static String CreateHotlineAPI=DOMAIN_NAME+"/api/hotline/create";
    private static String VerifiyHotlineAPI=DOMAIN_NAME+"/api/hotline/verify";
    private static String GetUnsolvedAsAgentAPI=DOMAIN_NAME+"/api/unsolved/agent";
    private static String GetUnsolvedAsMasterAPI=DOMAIN_NAME+"/api/unsolved/manage";
    private static String ResponseSolvingAPI=DOMAIN_NAME+"/api/solve";
    private static String ShowHotlineInfoAPI=DOMAIN_NAME+"/api/hotline/info";
    private static String RequestUpgradeHotlineAPI=DOMAIN_NAME+"/api/hotline/upgrade";

    public static String getResponseSolvingAPI() {
        return ResponseSolvingAPI;
    }

    public static String getGetUnsolvedAsAgentAPI() {
        return GetUnsolvedAsAgentAPI;
    }

    public static String getShowHotlineInfoAPI() {
        return ShowHotlineInfoAPI;
    }

    public static String getCreatCallLogAPI() {
        return CreatCallLogAPI;
    }

    public static String getShowCallogsAPI() {
        return ShowCallogsAPI;
    }

    public static String getSignInAPI(){
        return SignInAPI;
    }

    public static String getSignUpAPI(){
        return SignUpAPI;
    }

    public static String getSignOutAPI(){
        return SignOutAPI;
    }

    public static String getGetListHotlineToBuy(String country) {
        return GetListHotlineToBuy+country;
    }

    public static String getRequestUpgradeHotlineAPI() {
        return RequestUpgradeHotlineAPI;
    }

    public static String getInvitationAPI() {
        return InvitationAPI;
    }

    public static String getShowHotlineAPI(){
        return ShowHotlineAPI;
    }

    public static String getRespondInvitationAPI(String hotline){
        return PREFIX_API+hotline+"/respond";
    }

    public static String getUnsolvedFilter(String hotline){
        return PREFIX_API+"unsolved/"+hotline;
    }
    public static String getShowInsideHotlineAPI(String hotline){
        return PREFIX_API+hotline;
    }

    public static String getAddSubgroupAPI(String hotline){
        return PREFIX_API+hotline+"/add/subgroup";
    }

    public static String getAddAgentAPI(String hotline){
        return PREFIX_API+hotline+"/add/agent";
    }

    public static String getBuyHotlineAPI(){
        return BuyHotlineAPI;
    }

    public static String getShowAgentHotlineAPI() {
        return ShowAgentHotlineAPI;
    }

    public static String getDeleteAgentAPI(String hotline){
        return PREFIX_API+hotline+"/delete/agent";
    }

    public static String getDeleteGroupAPI(String hotline){
        return PREFIX_API+hotline+"/delete";
    }

    public static String getChangePasswordAPI() {
        return ChangePasswordAPI;
    }

    public static String getCreateHotlineAPI() {
        return CreateHotlineAPI;
    }

    public static String getVerifiyHotlineAPI() {
        return VerifiyHotlineAPI;
    }
}
