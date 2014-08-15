
//
//  MenuTableViewController.m
//  CampusMovil
//
//  Created by Daniel Klinkert on 8/8/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import "MenuTableViewController.h"

@interface MenuTableViewController ()

@end

@implementation MenuTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    markersArray = [[NSMutableArray alloc] init];

    CMCoreService * markerService = [[CMCoreService alloc] init];
    [markerService setDelegate:self];
    [markerService bringAllMarkers];
    
    markers = [[NSMutableDictionary alloc] init];
    
    dataSource = [[NSMutableArray alloc] initWithObjects:@"Suggestions",@"About Us",@"Settings",@"LogOut", nil];
   // markers = [[NSMutableArray alloc]initWithObjects:@"marker1", nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    if (section == 0 ) {
        return [markersArray count];
    }else{
        return [dataSource count];
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section{
    
    UILabel * title = [[UILabel alloc] initWithFrame:CGRectMake(6, 3, 136, 2)];
    [title setTextAlignment:NSTextAlignmentRight];
    
    if (section == 0) {
        [title setText:@"Markers"];
    }else{
        [title setText:@"Options"];
    }
    
    return title;
    
}

-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 30;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    CustomTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];
    
    // Configure the cell...
    
    
    if (indexPath.section == 0){
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        
        [cell.customLabelText setText:[[markersArray objectAtIndex:indexPath.row] objectForKey:@"title"]];
        
        if ([[[markersArray objectAtIndex:indexPath.row] objectForKey:@"category"] isEqualToString:@"biblioteca"]) {
            cell.customImageView.image = [UIImage imageNamed:@"library"];
        }else if ([[[markersArray objectAtIndex:indexPath.row] objectForKey:@"category"] isEqualToString:@"auditorio"]){
            cell.customImageView.image = [UIImage imageNamed:@"auditorium"];
        }else if ([[[markersArray objectAtIndex:indexPath.row] objectForKey:@"category"] isEqualToString:@"bloque"]){
            cell.customImageView.image = [UIImage imageNamed:@"block"];
        }else if ([[[markersArray objectAtIndex:indexPath.row] objectForKey:@"category"] isEqualToString:@"cec"]){
            cell.customImageView.image = [UIImage imageNamed:@"cec"];
        }
        
        
        
    }else if (indexPath.section ==1){
        
        
        [cell.customLabelText setText:[dataSource objectAtIndex:indexPath.row]];
    }
    
    [cell.textLabel setTextAlignment:NSTextAlignmentRight];
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    
    if (indexPath.section == 0) {
        
        UIStoryboard *main= [UIStoryboard storyboardWithName:@"Main" bundle:nil];
        
        UIViewController *vc = [main instantiateViewControllerWithIdentifier:@"DetailController"];
        
        
        [[SlideNavigationController sharedInstance] pushViewController:vc animated:true];
    }else if (indexPath.section == 1){
        
        switch (indexPath.row) {
            case 0:
                
                break;
            case 1:
                break;
            case 2:
                [self performSegueWithIdentifier:@"settings_Segue" sender:nil];
                break;
            case 3:
                break;
                
            default:
                
                break;
        }
    }
    
    
}

#pragma Service

- (void)CMCore:(CMCoreService *)cm didReciveResponse:(NSDictionary *)dict{
    for (NSDictionary *allMarkers in dict) {
       
        
        
        [markersArray addObject:allMarkers];
        
        
       
        //[markers setObject:[allMarkers objectForKey:@"title"] forKey:[allMarkers objectForKey:@"subtitle"]];
    
        NSLog(@"%@",markersArray);
    }
    
  
    
   
    [self.tableView reloadData];
    
}

/*
 // Override to support conditional editing of the table view.
 - (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
 // Return NO if you do not want the specified item to be editable.
 return YES;
 }
 */

/*
 // Override to support editing the table view.
 - (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
 if (editingStyle == UITableViewCellEditingStyleDelete) {
 // Delete the row from the data source
 [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
 } else if (editingStyle == UITableViewCellEditingStyleInsert) {
 // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
 }
 }
 */

/*
 // Override to support rearranging the table view.
 - (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
 }
 */

/*
 // Override to support conditional rearranging of the table view.
 - (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
 // Return NO if you do not want the item to be re-orderable.
 return YES;
 }
 */


 #pragma mark - Navigation
 
 // In a storyboard-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
      UIStoryboard *main= [UIStoryboard storyboardWithName:@"Main" bundle:nil];
     if ([segue.identifier isEqualToString:@"settings_Segue"]) {
        
         
         UIViewController *vc = [main instantiateViewControllerWithIdentifier:@"SettingsTableViewController"];
         
         
         [[SlideNavigationController sharedInstance] pushViewController:vc animated:true];
     }
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }


@end
