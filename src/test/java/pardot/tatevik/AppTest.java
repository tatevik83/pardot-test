package pardot.tatevik;

import java.util.List;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * WebDriver test for pardot
 * 
 *-----------TEST CASES----------------
 * 
Log in to Pardot (https://pi.pardot.com, Username: pardot.applicant@pardot.com, Password: Applicant2012)
Create a list with a random name (Marketing > Segmentation > Lists)
Attempt to create another list with that same name and ensure the system correctly gives a validation failure
Rename the original list
Ensure the system allows the creation of another list with the original name now that the original list is renamed
Create a new prospect (Prospect > Prospect List)
Add your new prospect to the newly created list
Ensure the new prospect is successfully added to the list upon save
Send a text only email to the list (Marketing > Emails)  *Please note, email is disabled in this account so you will not actually be able to send the email.  This is okay.
Log out

 */
public class AppTest extends TestCase
{
	private final String URL = "https://pi.pardot.com"; 
	private final String LOGIN = "pardot.applicant@pardot.com";
	private final String PASSWORD = "Applicant2012"; 
	private final int LONG_PERIOD = 10000;
	private final int SHORT_PERIOD = 2000;
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * WebDriver test for pardot
     */
	public void testApp() {
		WebDriver driver = null;

		try {
			// Using CHROME WebDriver
			System.setProperty("webdriver.chrome.driver",
					System.getProperty("user.dir") + "/src/test/resources/chromedriver");

			driver = new ChromeDriver();
			driver.get(URL);

			login(driver); // login
			
			// starts with 11 so that the search for list with this name is not exhaustive 
			String name = "11tatevik_" + UUID.randomUUID().toString();

			// create the first list
			navigateToList(driver);
			createList(driver, name, true);

			// repeat the process
			navigateToList(driver);
			createList(driver, name, false);

			// verify that validation fails and right error messages aredisplayed
			verifyErrorMessagesOnListSave(driver);

			// cancel and goto record home for edit
			driver.findElement(By.linkText("Cancel")).click();
			driver.findElement(By.linkText(name)).click();
			waitForHeader(driver, "h1", name);
			editList(driver, name);

			// if this line is reached then edit was successful
			
			// as the name is changed, the following create should still succeed
			navigateToList(driver);
			createList(driver, name, true);
			
			// if this line is reached then create was successful

			// create and validate a prospect
			navigateToProspects(driver);
			createProspect(driver, name + "@gmail.com", name);
			validateListAddedToProspect(driver, name);

			// create email
			navigateToEmails(driver);
			createEmail(driver, name);

			logout(driver); // logout
		} catch (Exception x) {
			Assert.fail("Test failed with unexpected error: " + x.getMessage());
		} finally {
			if (driver != null) {
				driver.close();
				driver.quit();
			}
		}
	}
	
	/**
	 * Verify that correct error messages are displayed when List is saved with duplicate name
	 * 
	 * @param driver CHROME WebDriver 
	 */
	private void verifyErrorMessagesOnListSave(WebDriver driver) {
		List<WebElement> errorElems = driver.findElements(By.cssSelector(".modal-dialog .alert-error"));
		Assert.assertTrue("Error messages printed should be 2", errorElems.size() == 2);
		Assert.assertEquals("Unexpected error text", errorElems.get(0).getText(),
				"Please correct the errors below and re-submit");
		Assert.assertEquals("Unexpected error text", errorElems.get(1).getText(),
				"Name\nPlease input a unique value for this field");
	}
    
	/**
	 * Use web driver to login to pardot org
	 * 
	 * @param driver CHROME WebDriver
	 */
    private void login(WebDriver driver) {
    	driver.findElement(By.id("email_address")).sendKeys(LOGIN);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);     
        driver.findElement(By.name("commit")).submit();
    }
    
    /**
     * Use web driver to navigate to pardot's emails section
     * 
     * @param driver CHROME WebDriver
     */
    private void navigateToEmails(WebDriver driver) {
    	
    	Actions actions = new Actions(driver);
        WebElement menuHoverLink = driver.findElement(By.id("mark-tog"));
        actions.moveToElement(menuHoverLink).build().perform();
        
        WebElement subLink1 = driver.findElement(By.linkText("Emails"));
        actions.moveToElement(subLink1).build().perform();
        
        waitForElement(driver, By.linkText("New Email"));
        
        WebElement subLink2 = driver.findElement(By.linkText("New Email"));
        actions.moveToElement(subLink2);
        actions.click();
        actions.perform();
        
        // wait for the header to turn to emails section header
        waitForHeader(driver, "h4", "Basic Email Information");
    }
    
    /**
     * Use web driver to logout from pardot org 
     * 
     * @param driver CHROME WebDriver
     */
    private void logout(WebDriver driver) {
    	Actions actions = new Actions(driver);
        WebElement userIcon = driver.findElement(By.className("icon-user"));
        actions.moveToElement(userIcon).build().perform();
        
        WebElement subLink = driver.findElement(By.className("icon-signout"));
        actions.moveToElement(subLink).click(); 
        actions.perform(); 
    }
    
    /**
     * Use web driver to navigate to the Prospects section of the org
     * 
     * @param driver CHROME WebDriver
     */
    private void navigateToProspects(WebDriver driver) {
    	Actions actions = new Actions(driver);
        WebElement menuHoverLink = driver.findElement(By.id("pro-tog"));
        actions.moveToElement(menuHoverLink).build().perform();
        
        WebElement subLink = driver.findElement(By.linkText("Prospect List"));
        actions.moveToElement(subLink).click(); 
        actions.perform();
        
        waitForHeader(driver, "h1", "Prospects");
        
    }
    
    /**
     * Use web driver to navigate to the Lists section of the org
     * 
     * @param driver CHROME WebDriver
     */
    private void navigateToList(WebDriver driver) {
    	Actions actions = new Actions(driver);
        WebElement menuHoverLink = driver.findElement(By.id("mark-tog"));
        actions.moveToElement(menuHoverLink).build().perform();

        WebElement subLink1 = driver.findElement(By.linkText("Segmentation"));
        actions.moveToElement(subLink1).build().perform();
        
        waitForElement(driver, By.linkText("Lists"));
        
        WebElement subLink2 = driver.findElement(By.linkText("Lists"));
        actions.moveToElement(subLink2);
        actions.click();
        actions.perform();
        
        // wait for the header to turn to Lists
        waitForHeader(driver, "h1", "Lists");
        
    }
    
    /**
     * Use web driver to validate that given list is added to prospect that is currently displayed
     * 
     * @param driver CHROME WebDriver
     * @param listName Name of the List 
     */
    private void validateListAddedToProspect(WebDriver driver, String listName) {
    	
    	List<WebElement> elems = driver.findElements(By.xpath("//a[contains(text(),'Lists')]")); 
    	for (WebElement element : elems) {
    		if(element.isDisplayed()) {
    			element.click();
    			break;
    		}
    	}
    	
    	waitForHeader(driver, "h3", "List Memberships"); 
    	
    	List<WebElement> elements = driver.findElements(By.cssSelector(".selected-lists li")); 
    	
    	for (WebElement element : elements) {
    		if (element.getText().equals(listName)) {
    			return; 
    		}
    	}
    	
    	Assert.fail("The list " + listName + " is not found in newly created prospect");
    	
    }
    
    
    /**
     * Use web driver to create an email and add given list to it
     * 
     * @param driver CHROME WebDriver
     * @param listName Name of the list to be added to the email
     * @throws InterruptedException
     */
    private void createEmail(WebDriver driver, String listName) throws InterruptedException {
    	driver.findElement(By.id("name")).sendKeys("Email from Tatevik");
    	
    	driver.findElement(By.className("asset-chooser")).click();
    	//waitForHeader(driver, "h3", "Select A Campaign");
    	waitForElement(driver, By.cssSelector("#folder-contents .folder-list-item"));
    	
    	WebElement elem = driver.findElement(By.cssSelector("#folder-contents .folder-list-item")); 
    	elem.click();
    	driver.findElement(By.id("select-asset")).click();
    	
    	By byCss = By.cssSelector("[id='email_type_text_only'][type='radio']");
    	driver.findElement(byCss).click();
    	
    	driver.findElement(By.id("save_information")).click();
    	waitForElement(driver, By.id("cancel_template")); 
    	
    	driver.findElement(By.id("cancel_template")).click();
    	waitForElement(driver, By.id("save_footer")); 
    	Thread.sleep(2000);
    	driver.findElement(By.id("save_footer")).click();
    	
    	waitForElement(driver, By.id("flow_sending"));
    	driver.findElement(By.id("flow_sending")).click();
    	
    	waitForElement(driver, By.id("email-wizard-list-select"));
    	
    	driver.findElement(By.id("email-wizard-list-select")).click();
    	
    	WebElement searchBox = driver.findElement(By.cssSelector("#email-wizard-list-select input[type='text']")); 
    	searchBox.sendKeys(listName);
    	
    	List<WebElement> entries = driver.findElements(By.cssSelector("#email-wizard-list-select ul li"));
        for (WebElement entry: entries) {
        	if(entry.isDisplayed()) {
        		entry.click();
        		break;
        	}
        }
    	
    	selectOption(driver, By.name("a_sender[]"), "Specific User");
    	
    	driver.findElement(By.cssSelector("input[name='subject_a'][type='text']")).sendKeys("Email Subject");;
    	driver.findElement(By.id("save_footer")).click();
    	
    	sleep(); // Only here to see the email being saved
    	
    } 
    
    /**
     * Create a new prospect with given email and add given list to it
     * 
     * @param driver driver CHROME WebDriver
     * @param email Prospect email
     * @param listName List to add to prospect
     * @throws InterruptedException
     */
    private void createProspect(WebDriver driver, String email, String listName) throws InterruptedException {
    	driver.findElement(By.id("pr_link_create")).click();
        waitForElement(driver, By.id("email"));
        
        driver.findElement(By.id("email")).sendKeys(email);
        
        
        selectFirstOption(driver, By.id("campaign_id")); 
        selectFirstOption(driver, By.id("profile_id"));
        
        
        List<WebElement> elements = driver.findElements(By.cssSelector("h4")); 
        
        for (WebElement element : elements) {
        	if (element.getText() != null && element.getText().contains("Lists")) {
        		element.click();
        		waitForElement(driver, By.id("pr_fields_lists_wrapper_")); 
        		
        		break;
        	}
        }
        
        WebElement searchElem = driver.findElement(By.id("pr_fields_lists_wrapper_")); 
        searchElem.click();
        
        By by = By.cssSelector("#pr_fields_lists_wrapper_ input[type='text']");
        waitForElement(driver, by); 
        
        driver.findElement(by).sendKeys(listName);
        
        List<WebElement> entries = driver.findElements(By.cssSelector("#pr_fields_lists_wrapper_ ul li"));
        for (WebElement entry: entries) {
        	if(entry.isDisplayed()) {
        		entry.click();
        		break;
        	}
        }
        
        waitForElement(driver, By.name("commit"));
        driver.findElement(By.name("commit")).submit();
        sleep(); // TODO: @tatevik Find solution and remove. Here prevent weird behavior of Chrome WebDriver
        waitForHeader(driver, "h1", email);
        
    }
    
    /**
     * Use web driver to select the first option for given HTML select element
     * 
     * @param driver CHROME WebDriver
     * @param by By locator for HTML select element 
     */
    private void selectFirstOption(WebDriver driver, By by) {
    	Select dropdown = new Select(driver.findElement(by));
    	dropdown.selectByIndex(1);
    }  
    
    /**
     * Use web driver to select given option for given HTML select element
     * 
     * @param driver CHROME WebDriver
     * @param by By locator for HTML select element 
     * @param optionName Option name
     */
    private void selectOption (WebDriver driver, By by, String optionName) {
    	WebElement select = driver.findElement(by);
        List<WebElement> groups = select.findElements(By.tagName("optgroup"));
        
        if (groups != null && groups.size() > 0) {
        	for (WebElement group : groups) {
        		List<WebElement> options = group.findElements(By.tagName("option")); 
        		
        		for (WebElement option : options) {
        			if(option.getText().equals(optionName)) {
                        option.click();
                        return;
                    }
        		}
            }
        } else {
        	List<WebElement> options = select.findElements(By.tagName("option")); 
    		
        	if (options != null && !options.isEmpty()) {
    		for (WebElement option : options) {
    			if(optionName.equals(option.getAttribute("text"))) {
                    option.click();
                    return;
                }
    		}
        	}
        }
        
        Assert.fail("Option " + optionName + " is missing from options");
    }

    /**
     * Use web driver to create List with given name
     * 
     * @param driver CHROME WebDriver
     * @param name Name of the list 
     * @param wait {@Boolean} indicator to wait and validate if record home is displayed
     */
    private void createList(WebDriver driver, String name, boolean validateHeader) {
        driver.findElement(By.id("listxistx_link_create")).click();
        waitForElement(driver, By.id("information_modal"));

        driver.findElement(By.id("name")).sendKeys(name);
        driver.findElement(By.id("save_information")).submit();
        if (validateHeader) {
        	waitForHeader(driver, "h1", name);
        }
    }
    
    /**
     * Use web driver to edit given list and append "_Edited" to its name
     * 
     * @param driver CHROME WebDriver
     * @param name List name
     */
    private void editList(WebDriver driver, String name) {
    	driver.findElement(By.linkText("Edit")).click();
    	waitForElement(driver, By.id("name")); 
    	
        driver.findElement(By.id("name")).sendKeys("_Edited");
        driver.findElement(By.id("save_information")).submit();
        
        waitForHeader(driver, "h1", name + "_Edited");
    }
    
    /**
     * Use web driver to wait until header with given type and text is displayed
     * 
     * @param driver CHROME WebDriver
     * @param headerType Type of the header to wait for, e.g h1
     * @param headerText Test of the header to wait for
     */
    private void waitForHeader(WebDriver driver, String headerType, String headerText) {
    	WebDriverWait wait = new WebDriverWait(driver, LONG_PERIOD);
        wait.until(ExpectedConditions.attributeContains(driver.findElement(By.cssSelector(headerType)), "innerHTML", headerText));
    }
    
    /**
     * Use web driver to wait until HTML element with given locator is displayed
     * 
     * @param driver CHROME WebDriver
     * @param by By locator for given HTML element 
     */
    private void waitForElement(WebDriver driver, By by) {
    	WebDriverWait wait = new WebDriverWait(driver, LONG_PERIOD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }
    
    /**
     * Introduce a short wait 
     * 
     * @throws InterruptedException
     */
    private void sleep() throws InterruptedException {
    	Thread.sleep(SHORT_PERIOD);
    }
}
