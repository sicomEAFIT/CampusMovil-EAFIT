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
            
            [delegate CMCore:self didReciveResponse:dict];
        }
        
        if (connectionError) {
            [delegate CMCore:self didError:connectionError];
        }
    }];
}

- (void)registerWithUsername:(NSString *)username password:(NSString *)password email:(NSString *)email{
    NSDictionary * sender = @{@"user":@{@"username":username,@"password":password,@"email":email}};
    
    [request setURL:[NSURL URLWithString:@"http://campusmovilapp.herokuapp.com/api/v1/register"]];
    [request setHTTPBody:[self request:sender]];
    [request setHTTPMethod:@"POST"];
    [self send];
    
}

- (void)bringAllMarkersWithUserName:(NSString *)username{
    [request setURL:[NSURL URLWithString:@"http://campusmovilapp.herokuapp.com/api/v1/markers?auth=3e48e2d68d9ad6caefc37b517cd788a1b7a2f656ac89a8d554225bc86a07014222d20baad211193862da8f1241eb273afcfecf73b77c5afb34c062203adce831"]];
    [request setHTTPMethod:@"GET"];
    [self send];
    
}



@end
