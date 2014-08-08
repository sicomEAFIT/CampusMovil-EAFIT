//
//  CMCoreService.m
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/5/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import "CMCoreService.h"

@implementation CMCoreService
@synthesize request;
@synthesize delegate;
@synthesize user;

#pragma mark - Singleton Instance

+ (id)sharedInstance {
    static CMCoreService * __instance;
    static dispatch_once_t __instance_token;
    
    dispatch_once(&__instance_token, ^{
        __instance = [[CMCoreService alloc] init];
    });
    
    return __instance;
}

#pragma mark - Metodos de la clase

- (instancetype)init {
    self = [super init];
    
    if (self) {
        request = [[NSMutableURLRequest alloc] init];
        [request setTimeoutInterval:10];
        [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
        [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
        
        NSDictionary * loggedUser = (NSDictionary *)[[NSUserDefaults standardUserDefaults] objectForKey:@"CMLoggedUser"];
        
        if (loggedUser) {
            user = [[CMUser alloc] initWithToken:[loggedUser objectForKey:@"token"]
                                        lifetime:[loggedUser objectForKey:@"expires"]];
            
            [user setUsername:[[loggedUser objectForKey:@"user"] objectForKey:@"username"]];
            [user setEmail:[[loggedUser objectForKey:@"user"] objectForKey:@"email"]];
            
            [self assignHTTPTokenAuth];
            
            NSLog(@"Using tokken for user: %@", user.username);
        }
    }
    
    return self;
}

- (NSData *)request:(NSDictionary *)dic {
    return [NSJSONSerialization dataWithJSONObject:dic options:kNilOptions error:nil];
}

- (void)loginWithUsername:(NSString *)username password:(NSString *)password {
    NSDictionary * sender = @{@"user": @{@"username": username, @"password": password}};
    
    [request setURL:[NSURL URLWithString:@"http://campusmovilapp.herokuapp.com/api/v1/auth"]];
    [request setHTTPBody:[self request:sender]];
    [request setHTTPMethod:@"POST"];
    
    [self send];
}

- (void)send {
    NSOperationQueue *queue = [NSOperationQueue mainQueue];
    
    [NSURLConnection sendAsynchronousRequest:request queue:queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *connectionError) {
        
        if ([data length] > 0 && connectionError == nil) {
            NSError * error;
            
            NSDictionary * dict = [NSJSONSerialization JSONObjectWithData:data
                                                                  options:NSJSONReadingMutableContainers|NSJSONReadingAllowFragments
                                                                    error:&error];
            if (error) {
                [delegate CMCore:self didError:error];
                return;
            }
            
            NSDictionary * auth;
            
            @try {
                if ((auth = [dict objectForKey:@"auth"]) != nil) {
                    NSString * token = [auth objectForKey:@"token"];
                    
                    NSDateFormatter *lt = [[NSDateFormatter alloc] init];
                    [lt setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZZ"];
                    
                    NSString *lifetime = [auth objectForKey:@"expires"];
                    
                    CMUser * newUser = [[CMUser alloc] initWithToken:token lifetime:[lt dateFromString:lifetime]];
                    [newUser setUsername:[[auth objectForKey:@"user"] objectForKey:@"username"]];
                    [newUser setEmail:[[auth objectForKey:@"user"] objectForKey:@"email"]];
                    
                    user = newUser;
                    
                    if (user) {
                        [[NSUserDefaults standardUserDefaults] setObject:auth forKey:@"CMLoggedUser"];
                    }
                    
                    [self assignHTTPTokenAuth];
                }
            }
            @catch (NSException *exception) { }
            @finally { }
            
            
            [delegate CMCore:self didReciveResponse:dict];
        }
        
        if (connectionError) {
            [delegate CMCore:self didError:connectionError];
        }
    }];
}

- (void)assignHTTPTokenAuth {
    if (user && user.token) {
        [request setValue:[NSString stringWithFormat:@"Token %@", user.token] forHTTPHeaderField:@"Authorization"];
    }
}

- (void)registerWithUsername:(NSString *)username password:(NSString *)password email:(NSString *)email{
    NSDictionary * sender = @{@"user":@{@"username":username,@"password":password,@"email":email}};
    
    [request setURL:[NSURL URLWithString:@"http://campusmovilapp.herokuapp.com/api/v1/register"]];
    [request setHTTPBody:[self request:sender]];
    [request setHTTPMethod:@"POST"];
    
    [self send];
}

- (void)bringAllMarkers {
    [request setURL:[NSURL URLWithString:@"http://campusmovilapp.herokuapp.com/api/v1/markers"]];
    [request setHTTPMethod:@"GET"];
    
    [self send];
}

+ (BOOL)isUserLogged {
    return [[NSUserDefaults standardUserDefaults] objectForKey:@"CMLoggedUser"] != nil;
}



@end
